package ru.dyatel.tsuschedule.data;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.widget.Toast;
import ru.dyatel.tsuschedule.R;
import ru.dyatel.tsuschedule.parsing.Lesson;
import ru.dyatel.tsuschedule.parsing.Parser;
import ru.dyatel.tsuschedule.parsing.SubgroupFilter;
import ru.dyatel.tsuschedule.util.IterableFilter;

import java.util.HashSet;
import java.util.Set;

public class DataFragment extends Fragment implements DataListener {

    public static final String TAG = "data";

    private static final String GROUP_ARGUMENT = "group";
    private static final String SUBGROUP_ARGUMENT = "subgroup";

    private String group;
    private int subgroup;

    private SavedDataDAO dataDAO;
    private Set<Lesson> lessons;

    private Set<DataListener> dataRequests = new HashSet<>();
    private Set<DataListener> listeners = new HashSet<>();

    public static DataFragment newInstance(String group, int subgroup) {
        DataFragment fragment = new DataFragment();

        Bundle bundle = new Bundle();
        bundle.putString(GROUP_ARGUMENT, group);
        bundle.putInt(SUBGROUP_ARGUMENT, subgroup);
        fragment.setArguments(bundle);

        return fragment;
    }

    public DataFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);

        Bundle arguments = getArguments();
        group = arguments.getString(GROUP_ARGUMENT);
        subgroup = arguments.getInt(SUBGROUP_ARGUMENT);

        dataDAO = new SavedDataDAO(getActivity().getApplication());
        dataDAO.load(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        dataDAO.save(lessons);
    }

    public void addListener(DataListener listener) {
        listeners.add(listener);
    }

    public void removeListener(DataListener listener) {
        listeners.remove(listener);
    }

    public void requestData(DataListener requester) {
        if (lessons == null) {
            if (!listeners.contains(requester)) {
                dataRequests.add(requester);
            }
        } else {
            requester.beforeDataUpdate();
            requester.onDataUpdate(filterData(lessons));
            requester.afterDataUpdate();
        }
    }

    private Set<Lesson> filterData(Set<Lesson> data) {
        IterableFilter<Lesson> filter = new IterableFilter<>();
        filter.apply(new SubgroupFilter(subgroup));
        return filter.filter(data);
    }

    @Override
    public void beforeDataUpdate() {
        for (DataListener l : dataRequests) l.beforeDataUpdate();
        for (DataListener l : listeners) l.beforeDataUpdate();
    }

    @Override
    public void onDataUpdate(Set<Lesson> data) {
        lessons = data;

        Set<Lesson> filtered = filterData(data);
        for (DataListener l : dataRequests) l.onDataUpdate(filtered);
        for (DataListener l : listeners) l.onDataUpdate(filtered);
    }

    @Override
    public void afterDataUpdate() {
        for (DataListener l : dataRequests) l.afterDataUpdate();
        for (DataListener l : listeners) l.afterDataUpdate();

        dataRequests.clear();
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public void setSubgroup(int subgroup) {
        if (this.subgroup != subgroup && lessons != null) {
            this.subgroup = subgroup;

            beforeDataUpdate();
            onDataUpdate(lessons);
            afterDataUpdate();
        } else {
            this.subgroup = subgroup;
        }
    }

    public String getGroup() {
        return group;
    }

    public int getSubgroup() {
        return subgroup;
    }

    public void fetchData() {
        new AsyncTask<Void, Void, Set<Lesson>>() {

            private int errorStringId;

            @Override
            protected void onPreExecute() {
                beforeDataUpdate();
            }

            @Override
            protected Set<Lesson> doInBackground(Void... params) {
                if (group == null || group.equals("")) {
                    errorStringId = R.string.no_group_index;
                    return null;
                }

                try {
                    return Parser.getLessons(group);
                } catch (IllegalArgumentException e) {
                    errorStringId = R.string.wrong_group_index;
                } catch (Exception e) {
                    errorStringId = R.string.load_failure;
                }
                return null;
            }

            @Override
            protected void onPostExecute(Set<Lesson> lessons) {
                if (lessons == null) {
                    Toast.makeText(
                            getActivity(),
                            errorStringId,
                            Toast.LENGTH_SHORT
                    ).show();
                } else {
                    onDataUpdate(lessons);
                }
                afterDataUpdate();
            }

        }.execute();
    }

}
