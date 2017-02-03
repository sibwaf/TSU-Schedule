package ru.dyatel.tsuschedule.layout;

import android.app.Activity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import ru.dyatel.tsuschedule.R;
import ru.dyatel.tsuschedule.parsing.Lesson;
import ru.dyatel.tsuschedule.util.Filter;
import ru.dyatel.tsuschedule.util.IterableFilter;
import ru.dyatel.tsuschedule.util.KeyExtractor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class WeekAdapter extends RecyclerView.Adapter<WeekAdapter.Holder> {

	private static final String[] normalWeekdayOrder = {
			"\u041F\u043E\u043D\u0435\u0434\u0435\u043B\u044C\u043D\u0438\u043A",
			"\u0412\u0442\u043E\u0440\u043D\u0438\u043A",
			"\u0421\u0440\u0435\u0434\u0430",
			"\u0427\u0435\u0442\u0432\u0435\u0440\u0433",
			"\u041F\u044F\u0442\u043D\u0438\u0446\u0430",
			"\u0421\u0443\u0431\u0431\u043E\u0442\u0430",
			"\u0412\u043E\u0441\u043A\u0440\u0435\u0441\u0435\u043D\u044C\u0435"
	};
	private List<String> weekdayOrder = new ArrayList<>();

	private Map<String, List<Lesson>> weekdays = new HashMap<>();

	private Activity activity;

	public WeekAdapter(Activity activity) {
		this.activity = activity;
	}

	static class Holder extends RecyclerView.ViewHolder {

		TextView weekday;
		RecyclerView list;

		Holder(View v, Activity activity) {
			super(v);

			weekday = (TextView) v.findViewById(R.id.weekday);
			list = (RecyclerView) v.findViewById(R.id.lesson_list);
			list.setLayoutManager(new LinearLayoutManager(v.getContext()));
			list.setAdapter(new WeekdayAdapter(activity));
		}

	}

	@Override
	public Holder onCreateViewHolder(ViewGroup parent, int viewType) {
		View v = LayoutInflater.from(parent.getContext())
				.inflate(R.layout.weekday, parent, false);
		return new Holder(v, activity);
	}

	@Override
	public void onBindViewHolder(Holder holder, int position) {
		String weekday = weekdayOrder.get(position);

		holder.weekday.setText(weekday);

		WeekdayAdapter adapter = (WeekdayAdapter) holder.list.getAdapter();
		adapter.updateData(weekdays.get(weekday));
	}

	@Override
	public int getItemCount() {
		return weekdays.size();
	}

	public void updateData(List<Lesson> lessons) {
		weekdayOrder.clear();
		weekdays.clear();

		// Get all used weekdays
		Set<String> keys = new KeyExtractor<Lesson, String>() {
			@Override
			protected String getKey(Lesson element) {
				return element.getWeekday();
			}
		}.extract(lessons);

		// Skip a weekday if there is no lessons
		for (String s : normalWeekdayOrder) {
			if (keys.contains(s)) weekdayOrder.add(s);
		}

		for (final String key : keys) {
			IterableFilter<Lesson> filter = new IterableFilter<>();
			filter.apply(new Filter<Lesson>() {
				@Override
				public boolean accept(Lesson obj) {
					return obj.getWeekday().equals(key);
				}
			});
			weekdays.put(key, filter.filter(lessons));
		}

		activity.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				notifyDataSetChanged();
			}
		});
	}

}
