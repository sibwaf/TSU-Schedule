package ru.dyatel.tsuschedule;

import android.app.Fragment;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import ru.dyatel.tsuschedule.parsing.Lesson;
import ru.dyatel.tsuschedule.parsing.Parity;
import ru.dyatel.tsuschedule.parsing.Parser;
import ru.dyatel.tsuschedule.parsing.util.Filter;
import ru.dyatel.tsuschedule.parsing.util.IterableFilter;

import java.io.IOException;
import java.util.Set;

public class WeekFragment extends Fragment {

    private static final String PARITY_ARGUMENT = "parity";

    private IterableFilter<Lesson> filter = new IterableFilter<Lesson>();

    private Set<Lesson> lessonList;

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

        // TODO: replace with normal data retrieving
        try {
            lessonList = Parser.getLessons("221251");
        } catch (IOException e) {
            Log.e("WeekFragment", "Failed to get lesson list", e);
        }

        // Create required week filter
        final Parity p = (Parity) getArguments().getSerializable(PARITY_ARGUMENT);
        filter.apply(new Filter<Lesson>() {
            @Override
            public boolean accept(Lesson obj) {
                return obj.getParity().equals(Parity.BOTH) || obj.getParity().equals(p);
            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View root = inflater.inflate(R.layout.fragment_week, container, false);

        RecyclerView weekdayList = (RecyclerView) root.findViewById(R.id.weekday_list);
        weekdayList.setLayoutManager(new LinearLayoutManager(root.getContext()));
        weekdayList.setAdapter(new WeekAdapter());

        // TODO: replace with sending data after retrieving it
        ((WeekAdapter) weekdayList.getAdapter()).updateData(filter.filter(lessonList));

        return root;
    }

}
