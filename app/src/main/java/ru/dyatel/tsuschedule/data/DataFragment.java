package ru.dyatel.tsuschedule.data;

import android.app.Fragment;
import android.os.AsyncTask;
import android.os.Bundle;
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

    private String group;
    private int subgroup;

    private SavedDataDAO dataDAO;
    private Set<Lesson> lessons;

    private Set<DataListener> dataRequests = new HashSet<>();
    private Set<DataListener> listeners = new HashSet<>();

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
            requester.onDataUpdate(lessons);
            requester.afterDataUpdate();
        }
    }

    public DataFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);

        dataDAO = new SavedDataDAO(getActivity().getApplication());
        dataDAO.load(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        dataDAO.save(lessons);
    }

    @Override
    public void beforeDataUpdate() {
        for (DataListener l : dataRequests) l.beforeDataUpdate();
        for (DataListener l : listeners) l.beforeDataUpdate();
    }

    @Override
    public void onDataUpdate(Set<Lesson> data) {
        lessons = data;

        IterableFilter<Lesson> filter = new IterableFilter<>();
        filter.apply(new SubgroupFilter(subgroup));
        Set<Lesson> filtered = filter.filter(lessons);

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
        this.subgroup = subgroup;
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

    public void broadcastDataUpdate() {
        beforeDataUpdate();
        onDataUpdate(lessons);
        afterDataUpdate();
    }

}
