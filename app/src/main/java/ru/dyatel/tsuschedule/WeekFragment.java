package ru.dyatel.tsuschedule;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import ru.dyatel.tsuschedule.parsing.Parity;

public class WeekFragment extends Fragment {

    public static WeekFragment newInstance(Parity parity) {
        WeekFragment fragment = new WeekFragment();
        /*Bundle args = new Bundle();
        args.putSt(ARG_PARAM1, param1);
        fragment.setArguments(args);*/
        // TODO: send arguments
        return fragment;
    }

    public WeekFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // TODO: get arguments
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_week, container, false);
    }

}
