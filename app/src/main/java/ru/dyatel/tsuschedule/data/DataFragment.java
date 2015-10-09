package ru.dyatel.tsuschedule.data;

import android.app.Fragment;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.Toast;
import ru.dyatel.tsuschedule.R;
import ru.dyatel.tsuschedule.parsing.Lesson;
import ru.dyatel.tsuschedule.parsing.Parser;

import java.util.HashSet;
import java.util.Set;

public class DataFragment extends Fragment implements DataListener {

    public static final String TAG = "data";

    private String group;

    private SavedDataDAO dataDAO;
    private Set<Lesson> lessons;

    private Set<DataListener> listeners = new HashSet<DataListener>();

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
    public void onDestroy() {
        super.onDestroy();

        dataDAO.save(lessons);
    }

    @Override
    public void onDataUpdate(Set<Lesson> data) {
        lessons = data;
        broadcastDataUpdate();
    }

    @Override
    public void afterDataUpdate() {
        for (DataListener l : listeners) l.afterDataUpdate();
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public void fetchData() {
        new AsyncTask<String, Void, Set<Lesson>>() {

            private int errorStringId;

            @Override
            protected Set<Lesson> doInBackground(String... params) {
                if (group == null || group.equals("")) {
                    errorStringId = R.string.no_group_index;
                    return null;
                }

                try {
                    return Parser.getLessons(params[0]);
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
                    afterDataUpdate();
                } else {
                    onDataUpdate(lessons);
                }
            }

        }.execute(group);
    }

    public void addListener(DataListener listener) {
        listeners.add(listener);
    }

    public void clearListeners() {
        listeners.clear();
    }

    public void broadcastDataUpdate() {
        for (DataListener l : listeners) l.onDataUpdate(lessons);
        afterDataUpdate();
    }

}
