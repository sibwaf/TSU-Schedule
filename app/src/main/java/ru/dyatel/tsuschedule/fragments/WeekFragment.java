package ru.dyatel.tsuschedule.fragments;

import android.app.Fragment;
import android.os.Bundle;
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
import ru.dyatel.tsuschedule.util.Filter;
import ru.dyatel.tsuschedule.util.IterableFilter;

import java.util.Set;

public class WeekFragment extends Fragment implements DataListener {

    private static final String PARITY_ARGUMENT = "parity";

    private IterableFilter<Lesson> filter = new IterableFilter<>();

    private DataFragment dataFragment;

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
        final Parity p = (Parity) getArguments().getSerializable(PARITY_ARGUMENT);
        filter.apply(new Filter<Lesson>() {
            @Override
            public boolean accept(Lesson obj) {
                return obj.getParity().equals(Parity.BOTH) || obj.getParity().equals(p);
            }
        });

        dataFragment = (DataFragment) getFragmentManager()
                .findFragmentByTag(DataFragment.TAG);
        dataFragment.addListener(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        dataFragment.removeListener(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View root = inflater.inflate(R.layout.fragment_week, container, false);

        RecyclerView weekdayList = (RecyclerView) root.findViewById(R.id.weekday_list);
        weekdayList.setLayoutManager(new LinearLayoutManager(root.getContext()));
        weekdays = new WeekAdapter();
        weekdayList.setAdapter(weekdays);

        return root;
    }

    @Override
    public void beforeDataUpdate() {

    }

    @Override
    public void onDataUpdate(Set<Lesson> lessons) {
        weekdays.updateData(filter.filter(lessons));
    }

    @Override
    public void afterDataUpdate() {

    }

}
