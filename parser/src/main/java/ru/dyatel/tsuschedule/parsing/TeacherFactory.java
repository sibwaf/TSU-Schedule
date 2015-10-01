package ru.dyatel.tsuschedule.parsing;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TeacherFactory {

    private static final Pattern pattern = Pattern.compile("^(.*), (.*) , (.*)$");

    public static Set<Teacher> teacherPool = new HashSet<>();

    public static Teacher get(String teacher) {
        String name = "", education = "", department = "";
        Matcher m = pattern.matcher(teacher);
        if (m.matches()) {
            name = m.group(1);
            education = m.group(2);
            department = m.group(3);
        }

        for (Teacher t : teacherPool) {
            if (t.equals(name, education, department)) return t;
        }

        Teacher t = new Teacher(name, education, department);
        teacherPool.add(t);
        return t;
    }

}
