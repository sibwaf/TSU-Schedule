package ru.dyatel.tsuschedule.parsing;

public class Lesson {

    private Parity parity;
    private String weekday;
    private String time;

    private String discipline;
    private String auditory;
    private Teacher teacher;

    private int subgroup;

    public Lesson(Parity parity, String weekday, String time, String discipline, String auditory, Teacher teacher, int subgroup) {
        this.parity = parity;
        this.weekday = weekday;
        this.time = time;

        this.discipline = discipline;
        this.auditory = auditory;
        this.teacher = teacher;

        this.subgroup = subgroup;
    }

    public Parity getParity() {
        return parity;
    }

    public String getWeekday() {
        return weekday;
    }

    public String getTime() {
        return time;
    }

    public String getDiscipline() {
        return discipline;
    }

    public String getAuditory() {
        return auditory;
    }

    public Teacher getTeacher() {
        return teacher;
    }

    public int getSubgroup() {
        return subgroup;
    }

    @Override
    public String toString() {
        return "[" + parity + "] [" + time + "] [" + discipline + (subgroup != 0 ? "] [" + subgroup : "") + "] [" + auditory + "] [" + teacher + "]";
    }

    @Override
    public int hashCode() {
        return toString().hashCode();
    }

    @Override
    public boolean equals(Object o) {
        return (o instanceof Lesson) &&
                ((Lesson) o).parity.equals(parity) &&
                ((Lesson) o).weekday.equals(weekday) &&
                ((Lesson) o).time.equals(time) &&
                ((Lesson) o).discipline.equals(discipline) &&
                ((Lesson) o).auditory.equals(auditory) &&
                ((Lesson) o).teacher.equals(teacher);
    }

}
