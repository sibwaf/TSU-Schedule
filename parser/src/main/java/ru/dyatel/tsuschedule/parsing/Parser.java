package ru.dyatel.tsuschedule.parsing;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Parser {

    private static final String subgroupString = "\u043f/\u0433";

    private static final String evenParityString = "\u0447/\u043d";
    private static final String oddParityString = "\u043d/\u0079";

    private static final String practiceString = "\u041f\u0440";
    private static final String lectureString = "\u041b";
    private static final String laboratoryString = "\u041b\u0430\u0431";

    private static final Pattern subgroupPattern = Pattern.compile("^.*( \\((\\d)" + subgroupString + "\\))$");
    private static final Pattern typePattern = Pattern.compile("^(.*):.*$");

    public static Set<Lesson> getLessons(String group) throws IOException {
        Document response = Jsoup.connect("http://schedule.tsu.tula.ru/")
                .data("group", group)
                .get();

        Set<Lesson> lessons = new HashSet<Lesson>();
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

        Matcher m;

        // extract type
        Lesson.Type type = Lesson.Type.UNKNOWN;
        m = typePattern.matcher(discipline);
        if (m.matches()) {
            String typeString = m.group(1);
            if (typeString.equals(practiceString))
                type = Lesson.Type.PRACTICE;
            else if (typeString.equals(lectureString))
                type = Lesson.Type.LECTURE;
            else if (typeString.equals(laboratoryString))
                type = Lesson.Type.LABORATORY;
        }

        // extract subgroup
        int subgroup = 0;
        m = subgroupPattern.matcher(auditory);
        if (m.matches()) {
            subgroup = Integer.parseInt(m.group(2));
            auditory = auditory.replace(m.group(1), "");
        }

        return new Lesson(
                parity,
                weekday, time,
                discipline,
                auditory,
                TeacherFactory.get(teacher),
                type,
                subgroup
        );
    }

}
