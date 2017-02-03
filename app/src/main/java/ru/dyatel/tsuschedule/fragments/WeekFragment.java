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
import ru.dyatel.tsuschedule.R;
import ru.dyatel.tsuschedule.ScheduleApplication;
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

import java.util.List;

public class WeekFragment extends Fragment implements EventListener {

	private static final String PARITY_ARGUMENT = "parity";

	private IterableFilter<Lesson> filter = new IterableFilter<>();

	private SwipeRefreshLayout swipeRefresh;
	private WeekAdapter weekdays;

	private EventBus eventBus;
	private LessonDAO lessonDAO;

	public static WeekFragment newInstance(Parity parity) {
		Bundle arguments = new Bundle();
		arguments.putSerializable(PARITY_ARGUMENT, parity);

		WeekFragment fragment = new WeekFragment();
		fragment.setArguments(arguments);
		return fragment;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Parity p = (Parity) getArguments().getSerializable(PARITY_ARGUMENT);
		if (p == null) throw new IllegalArgumentException("No parity supplied!");
		filter.apply(new ParityFilter(p));

		Activity activity = getActivity();

		weekdays = new WeekAdapter(activity);

		ScheduleApplication application = (ScheduleApplication) activity.getApplication();
		eventBus = application.getEventBus();
		lessonDAO = application.getDatabaseManager().getLessonDAO();

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
		weekdayList.setLayoutManager(new LinearLayoutManager(getContext()));
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
	public void handleEvent(final Event type) {
		getActivity().runOnUiThread(new Runnable() {
			@Override
			public void run() {
				switch (type) {
					case DATA_UPDATED:
						new RefreshTask().execute();
						break;
					case DATA_UPDATE_FAILED:
						swipeRefresh.setRefreshing(false);
						break;
				}
			}
		});
	}

	private class RefreshTask extends AsyncTask<Void, Void, Void> {

		@Override
		protected void onPreExecute() {
			if (swipeRefresh != null) swipeRefresh.setRefreshing(true);
		}

		@Override
		protected Void doInBackground(Void... params) {
			List<Lesson> lessons = lessonDAO.request(DataPreferenceHelperKt.getSubgroup(getContext()));
			weekdays.updateData(filter.filter(lessons));
			return null;
		}

		@Override
		protected void onPostExecute(Void aVoid) {
			if (swipeRefresh != null) swipeRefresh.setRefreshing(false);
		}

	}

}
