package ru.dyatel.tsuschedule.fragments;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import org.jetbrains.annotations.NotNull;
import ru.dyatel.tsuschedule.ActivityUtilKt;
import ru.dyatel.tsuschedule.R;
import ru.dyatel.tsuschedule.data.DataPreferenceHelperKt;
import ru.dyatel.tsuschedule.data.LessonDAO;
import ru.dyatel.tsuschedule.data.LessonFetchTask;
import ru.dyatel.tsuschedule.events.Event;
import ru.dyatel.tsuschedule.events.EventBus;
import ru.dyatel.tsuschedule.events.EventListener;
import ru.dyatel.tsuschedule.layout.WeekAdapter;
import ru.dyatel.tsuschedule.parsing.Lesson;
import ru.dyatel.tsuschedule.parsing.Parity;
import ru.dyatel.tsuschedule.parsing.ParityFilter;
import ru.dyatel.tsuschedule.util.IterableFilter;

public class WeekFragment extends Fragment implements EventListener {

    private static final String PARITY_ARGUMENT = "parity";

    private IterableFilter<Lesson> filter = new IterableFilter<>();

    private SwipeRefreshLayout swipeRefresh;
    private WeekAdapter weekdays;

    private EventBus eventBus;
    private LessonDAO lessonDAO;

    public static WeekFragment newInstance(Parity parity) {
        WeekFragment fragment = new WeekFragment();
        Bundle arguments = new Bundle();
        arguments.putSerializable(PARITY_ARGUMENT, parity);
        fragment.setArguments(arguments);
        return fragment;
    }

    public WeekFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Create required week filter
        // If for some reason we didn't receive parity argument,
        // we will not filter by parity
        Parity p = (Parity) getArguments().getSerializable(PARITY_ARGUMENT);
        if (p != null) filter.apply(new ParityFilter(p));

        Activity activity = getActivity();

        weekdays = new WeekAdapter(activity);

        eventBus = ActivityUtilKt.getEventBus(activity);
        lessonDAO = ActivityUtilKt.getLessons(activity);

        eventBus.subscribe(this, Event.DATA_UPDATED, Event.DATA_UPDATE_FAILED);

        new RefreshTask().execute();
    }

    @Override
    public void onDestroy() {
        eventBus.unsubscribe(this);
        super.onDestroy();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View root = inflater.inflate(R.layout.week_fragment, container, false);

        RecyclerView weekdayList = (RecyclerView) root.findViewById(R.id.weekday_list);
        weekdayList.setLayoutManager(new LinearLayoutManager(root.getContext()));
        weekdayList.setAdapter(weekdays);

        // Wire up the SwipeRefreshLayout
        swipeRefresh = (SwipeRefreshLayout) root.findViewById(R.id.swipe_refresh);
        swipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                new LessonFetchTask(getContext(), eventBus, lessonDAO).execute();
            }
        });

        return root;
    }

    @Override
    public void handleEvent(@NotNull Event type) {
        switch (type) {
            case DATA_UPDATED:
                swipeRefresh.setRefreshing(true);
                new RefreshTask().execute();
                break;
            case DATA_UPDATE_FAILED:
                swipeRefresh.setRefreshing(false);
                break;
        }
    }

    private class RefreshTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {
            weekdays.updateData(filter.filter(
                    lessonDAO.request(DataPreferenceHelperKt.getSubgroup(getContext()))
            ));
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            swipeRefresh.setRefreshing(false);
        }

    }

}
