package ru.dyatel.tsuschedule;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import org.solovyev.android.views.llm.LinearLayoutManager;
import ru.dyatel.tsuschedule.parsing.Lesson;
import ru.dyatel.tsuschedule.parsing.util.Filter;
import ru.dyatel.tsuschedule.parsing.util.IterableFilter;
import ru.dyatel.tsuschedule.parsing.util.KeyExtractor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class WeekAdapter extends RecyclerView.Adapter<WeekAdapter.Holder> {

    // TODO: proper sorting by weekday
    private List<String> order = new ArrayList<String>();

    private Map<String, Set<Lesson>> weekdays = new HashMap<String, Set<Lesson>>();

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
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.weekday_layout, parent, false);
        return new Holder(v);
    }

    @Override
    public void onBindViewHolder(Holder holder, int position) {
        String weekday = order.get(position);

        holder.weekday.setText(weekday);
        WeekdayAdapter adapter = (WeekdayAdapter) holder.list.getAdapter();
        adapter.updateData(weekdays.get(weekday));
    }

    @Override
    public int getItemCount() {
        return weekdays.size();
    }

    public void updateData(Set<Lesson> lessons) {
        Set<String> keys = new KeyExtractor<Lesson, String>() {
            @Override
            protected String getKey(Lesson element) {
                return element.getWeekday();
            }
        }.extract(lessons);

        for (final String key : keys) {
            IterableFilter<Lesson> filter = new IterableFilter<Lesson>();
            filter.apply(new Filter<Lesson>() {
                @Override
                public boolean accept(Lesson obj) {
                    return obj.getWeekday().equals(key);
                }
            });
            weekdays.put(key, filter.filter(lessons));
            order.add(key); // TODO: remove this
        }

        notifyDataSetChanged();
    }

}
