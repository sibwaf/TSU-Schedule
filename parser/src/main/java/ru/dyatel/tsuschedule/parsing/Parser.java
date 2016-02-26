package ru.dyatel.tsuschedule.parsing;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Parser {

    private static final String practiceString = "\u041f\u0440";
    private static final String lectureString = "\u041b";
    private static final String laboratoryString = "\u041b\u0430\u0431";

    private static final String evenParityString = "\u0447/\u043d";
    private static final String oddParityString = "\u043d/\u043d";

    private static final Pattern teacherPattern = Pattern.compile("^(.+?),.*$");
    private static final Pattern typePattern = Pattern.compile("^(.+?):.*$");
    private static final Pattern subgroupPattern = Pattern.compile("^.*( \\((\\d).*?\\))$");

    private static Connection connection = Jsoup.connect("http://schedule.tsu.tula.ru/");

    public static void setTimeout(int timeout) {
        connection.timeout(timeout);
    }

    public static Set<Lesson> getLessons(String group) throws IOException {
        Document response = connection.data("group", group).get();

        if (response.getElementById("results").children().size() == 0)
            throw new IllegalArgumentException("Wrong group index: " + group);

        Set<Lesson> lessons = new HashSet<>();
        for (Element day : response.getElementById("results").children()) {
            String weekday = day.child(0).text();

            for (Element lesson : day.child(1).child(0).children()) {
                String time = lesson.child(0).text();

                Element lessonContainer = lesson.child(1);
                if (lessonContainer.child(0).tag().getName().equals("table")) {
                    // there will be only one lesson
                    String teacher = lessonContainer.getElementsByClass("teac").size() == 0 ?
                            "" : lessonContainer.getElementsByClass("teac").get(0).text();
                    lessons.add(constructLesson(weekday, time, teacher, Parity.BOTH, lessonContainer));
                } else {
                    // there will be two lessons (or more?)
                    int counter = 0;
                    while (counter < lessonContainer.children().size()) {
                        Element e = lessonContainer.child(counter);
                        if (e.classNames().contains("teac"))
                            throw new IllegalStateException("Can't parse: got \"teac\" div!");

                        String parityString = e.getElementsByClass("parity").get(0).text().trim();
                        Parity parity = Parity.BOTH;
                        if (parityString.equals(evenParityString)) parity = Parity.EVEN;
                        else if (parityString.equals(oddParityString)) parity = Parity.ODD;

                        // find teacher
                        String teacher = "";
                        Element next = e.nextElementSibling();
                        if (next != null && next.classNames().contains("teac")) {
                            teacher = next.text();
                            counter++;
                        }

                        lessons.add(constructLesson(weekday, time, teacher, parity, e));
                        counter++;
                    }
                }
            }
        }
        return lessons;
    }

    private static Lesson constructLesson(String weekday, String time, String teacher, Parity parity, Element e) {
        String discipline = e.getElementsByClass("disc").get(0).text().replace(",", "");
        String auditory = e.getElementsByClass("aud").get(0).text();

        // extract subgroup
        int subgroup = 0;
        Matcher m = subgroupPattern.matcher(auditory);
        if (m.matches()) {
            subgroup = Integer.parseInt(m.group(2));
            auditory = auditory.replace(m.group(1), "");
        }

        return new Lesson(
                parity,
                weekday, time,
                discipline,
                auditory,
                getTeacherName(teacher),
                getType(discipline),
                subgroup
        );
    }

    private static String getTeacherName(String teacher) {
        Matcher m = teacherPattern.matcher(teacher);
        if (m.matches()) {
            return m.group(1);
        }
        return "";
    }

    private static Lesson.Type getType(String discipline) {
        Matcher m = typePattern.matcher(discipline);
        if (m.matches()) {
            String typeString = m.group(1);
            if (typeString.equals(practiceString)) return Lesson.Type.PRACTICE;
            if (typeString.equals(lectureString)) return Lesson.Type.LECTURE;
            if (typeString.equals(laboratoryString)) return Lesson.Type.LABORATORY;
        }
        return Lesson.Type.UNKNOWN;
    }

}
