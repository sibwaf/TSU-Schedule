package ru.dyatel.tsuschedule.layout;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import ru.dyatel.tsuschedule.R;
import ru.dyatel.tsuschedule.parsing.Lesson;

import java.util.ArrayList;
import java.util.List;

public class WeekdayAdapter extends RecyclerView.Adapter<WeekdayAdapter.Holder> {

    private List<Lesson> lessons = new ArrayList<>();

    class Holder extends RecyclerView.ViewHolder {

        View color;

        TextView time;
        TextView auditory;
        TextView discipline;
        TextView teacher;

        public Holder(View v) {
            super(v);

            color = v.findViewById(R.id.color);

            time = (TextView) v.findViewById(R.id.time);
            auditory = (TextView) v.findViewById(R.id.auditory);
            discipline = (TextView) v.findViewById(R.id.discipline);
            teacher = (TextView) v.findViewById(R.id.teacher);
        }

    }

    @Override
    public Holder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new Holder(
                LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.lesson, parent, false)
        );
    }

    @Override
    public void onBindViewHolder(Holder holder, int position) {
        Lesson lesson = lessons.get(position);

        // Figure out the right color to use for color marker
        int colorResID;
        switch (lesson.getType()) {
            case PRACTICE:
                colorResID = R.color.practice_color;
                break;
            case LECTURE:
                colorResID = R.color.lecture_color;
                break;
            case LABORATORY:
                colorResID = R.color.laboratory_color;
                break;
            default:
                colorResID = R.color.unknown_color;
        }
        holder.color.setBackgroundResource(colorResID);

        holder.time.setText(lesson.getTime());
        holder.auditory.setText(lesson.getAuditory());
        holder.discipline.setText(lesson.getDiscipline());
        holder.teacher.setText(lesson.getTeacher());

        // Hide views if they do not contain any data
        holder.teacher.setVisibility(
                holder.teacher.getText().equals("") ? View.GONE : View.VISIBLE);
    }

    @Override
    public int getItemCount() {
        return lessons.size();
    }

    public void updateData(List<Lesson> lessons) {
        this.lessons = lessons;
        notifyDataSetChanged();
    }

}
