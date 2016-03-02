package ru.dyatel.tsuschedule.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import ru.dyatel.tsuschedule.R;
import ru.dyatel.tsuschedule.data.DataFragment;
import ru.dyatel.tsuschedule.data.DataListener;
import ru.dyatel.tsuschedule.layout.WeekAdapter;
import ru.dyatel.tsuschedule.parsing.Lesson;
import ru.dyatel.tsuschedule.parsing.Parity;
import ru.dyatel.tsuschedule.parsing.ParityFilter;
import ru.dyatel.tsuschedule.util.IterableFilter;

import java.util.Set;

public class WeekFragment extends Fragment implements DataListener {

    private static final String PARITY_ARGUMENT = "parity";

    private IterableFilter<Lesson> filter = new IterableFilter<>();

    private DataFragment dataFragment;

    private SwipeRefreshLayout swipeRefresh;
    private WeekAdapter weekdays;

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

        weekdays = new WeekAdapter();

        dataFragment = (DataFragment) getActivity().getSupportFragmentManager()
                .findFragmentByTag(DataFragment.TAG);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View root = inflater.inflate(R.layout.fragment_week, container, false);

        RecyclerView weekdayList = (RecyclerView) root.findViewById(R.id.weekday_list);
        weekdayList.setLayoutManager(new LinearLayoutManager(root.getContext()));
        weekdayList.setAdapter(weekdays);

        // Wire up the SwipeRefreshLayout
        swipeRefresh = (SwipeRefreshLayout) root.findViewById(R.id.swipe_refresh);
        swipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                dataFragment.fetchData();
            }
        });

        return root;
    }

    @Override
    public void onResume() {
        super.onResume();
        dataFragment.addListener(this);
        dataFragment.requestData(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        dataFragment.removeListener(this);
    }

    @Override
    public void beforeDataUpdate() {
        swipeRefresh.setRefreshing(true);
    }

    @Override
    public void onDataUpdate(Set<Lesson> lessons) {
        weekdays.updateData(filter.filter(lessons));
    }

    @Override
    public void afterDataUpdate() {
        swipeRefresh.setRefreshing(false);
    }

}
