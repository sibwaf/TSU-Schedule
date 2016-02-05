package ru.dyatel.tsuschedule.layout;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import org.solovyev.android.views.llm.LinearLayoutManager;
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
            "\u043f\u043e\u043d\u0435\u0434\u0435\u043b\u044c\u043d\u0438\u043a",
            "\u0432\u0442\u043e\u0440\u043d\u0438\u043a",
            "\u0441\u0440\u0435\u0434\u0430",
            "\u0447\u0435\u0442\u0432\u0435\u0440\u0433",
            "\u043f\u044f\u0442\u043d\u0438\u0446\u0430",
            "\u0441\u0443\u0431\u0431\u043e\u0442\u0430",
            "\u0432\u043e\u0441\u043a\u0440\u0435\u0441\u0435\u043d\u044c\u0435"
    };
    private List<String> weekdayOrder = new ArrayList<>();

    private Map<String, Set<Lesson>> weekdays = new HashMap<>();

    class Holder extends RecyclerView.ViewHolder {

        TextView weekday;
        RecyclerView list;

        public Holder(View v) {
            super(v);

            weekday = (TextView) v.findViewById(R.id.weekday);
            list = (RecyclerView) v.findViewById(R.id.lesson_list);
            list.setLayoutManager(new LinearLayoutManager(v.getContext()));
            list.setAdapter(new WeekdayAdapter());
        }

    }

    @Override
    public Holder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new Holder(
                LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.weekday_layout, parent, false)
        );
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

    public void updateData(Set<Lesson> lessons) {
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

        notifyDataSetChanged();
    }

}
