package ru.dyatel.tsuschedule;

import android.app.Fragment;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.Toast;
import ru.dyatel.tsuschedule.parsing.Lesson;
import ru.dyatel.tsuschedule.parsing.Parser;

import java.util.Collections;
import java.util.Set;

public class DataFragment extends Fragment {

    private Set<Lesson> lessons = Collections.emptySet();

    private Listener listener = null;

    public DataFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    public void fetchData(String group) {
        new AsyncTask<String, Void, Set<Lesson>>() {

            @Override
            protected Set<Lesson> doInBackground(String... params) {
                try {
                    return Parser.getLessons(params[0]);
                } catch (Exception e) {
                    // Do nothing
                }
                return null;
            }

            @Override
            protected void onPostExecute(Set<Lesson> lessons) {
                if (lessons == null)
                    Toast.makeText(
                            getActivity(),
                            R.string.load_failure,
                            Toast.LENGTH_SHORT
                    ).show();
                else {
                    DataFragment.this.lessons = lessons;
                    broadcastDataUpdate();
                }
            }

        }.execute(group);
    }

    public void setListener(Listener listener) {
        this.listener = listener;
    }

    public void broadcastDataUpdate() {
        if (listener != null) listener.onDataUpdate(lessons);
    }

    public interface Listener {

        void onDataUpdate(Set<Lesson> lessons);

    }

}
