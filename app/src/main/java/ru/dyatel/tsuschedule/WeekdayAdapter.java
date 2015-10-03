package ru.dyatel.tsuschedule;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import ru.dyatel.tsuschedule.parsing.Lesson;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class WeekdayAdapter extends RecyclerView.Adapter<WeekdayAdapter.Holder> {

    private List<Lesson> lessons = new ArrayList<Lesson>();

    class Holder extends RecyclerView.ViewHolder {

        View color;

        TextView discipline;
        TextView auditory;
        TextView teacher;

        public Holder(View v) {
            super(v);

            color = v.findViewById(R.id.color);

            discipline = (TextView) v.findViewById(R.id.discipline);
            auditory = (TextView) v.findViewById(R.id.auditory);
            teacher = (TextView) v.findViewById(R.id.teacher);
        }

    }

    @Override
    public Holder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.lesson_layout, parent, false);
        return new Holder(v);
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

        holder.discipline.setText(lesson.getDiscipline());
        holder.auditory.setText(lesson.getAuditory());
        holder.teacher.setText(lesson.getTeacher());
    }

    @Override
    public int getItemCount() {
        return lessons.size();
    }

    public void updateData(Set<Lesson> lessons) {
        this.lessons.clear();
        for (Lesson l : lessons) this.lessons.add(l);
        // TODO: sorting

        notifyDataSetChanged();
    }

}
