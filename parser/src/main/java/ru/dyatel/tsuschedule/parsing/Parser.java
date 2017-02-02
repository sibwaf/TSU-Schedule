package ru.dyatel.tsuschedule.parsing;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Parser {

	private static final String practiceString = "\u041F\u0440\u0430\u043A\u0442. \u0437\u0430\u043D\u044F\u0442\u0438\u044F";
	private static final String lectureString = "\u041B\u0435\u043A\u0446\u0438\u0438";
	private static final String laboratoryString = "\u041B\u0430\u0431. \u0437\u0430\u043D\u044F\u0442\u0438\u044F";

	private static final String evenParityString = "\u0447/\u043D";
	private static final String oddParityString = "\u043D/\u043D";

	private static final String subgroupString = "\u043F/\u0433\u0440";

	private static final Pattern timePattern = Pattern.compile("^(?:(\\S+?) )?(\\S+)$");
	private static final Pattern disciplinePattern = Pattern.compile("^(.+?)" +
			" \\((.+?)\\)" +
			"(?: \\((\\d) " + subgroupString + "\\))?$");

	private Connection connection = Jsoup.connect("http://schedule.tsu.tula.ru/");

	private String currentWeekday = null;

	public void setTimeout(int timeout) {
		connection.timeout(timeout);
	}

	public Set<Lesson> getLessons(String group) throws IOException {
		Document response = connection.data("group", group).get();
		Element result = response.getElementById("results");

		if (result.childNodeSize() == 0) throw new BadGroupException();

		Set<Lesson> lessons = new HashSet<>();

		for (Element lessonElement : result.children()) {
			Elements rows = lessonElement.child(0).child(0).children();
			// Ignore the padding row if it is present
			Element lessonDataElement = rows.size() == 1 ? rows.get(0) : rows.get(1);

			String time = extractTime(lessonDataElement.getElementsByClass("time").text().trim());
			if (currentWeekday == null) throw new ParsingException("Can't find weekday of the lesson");

			Parity parity = extractParity(lessonDataElement.getElementsByClass("parity").text().trim());

			String discipline;
			Lesson.Type type;
			int subgroup = 0;

			String disciplineString = lessonDataElement.getElementsByClass("disc").text().trim();
			if (disciplineString.endsWith(","))
				disciplineString = disciplineString.substring(0, disciplineString.length() - 1);

			Matcher disciplineMatcher = disciplinePattern.matcher(disciplineString);
			if (disciplineMatcher.matches()) {
				discipline = disciplineMatcher.group(1);
				type = extractType(disciplineMatcher.group(2));

				String subgroupText = disciplineMatcher.group(3);
				if (subgroupText != null)
					subgroup = Integer.parseInt(subgroupText);
			} else throw new ParsingException("Can't parse discipline string: " + disciplineString);

			String auditory = lessonDataElement.getElementsByClass("aud").text().trim();

			String teacher = "";
			Elements teacherElements = lessonDataElement.getElementsByClass("teac");
			if (teacherElements.size() == 1) teacher = teacherElements.text().trim();

			lessons.add(new Lesson(parity, currentWeekday, time, discipline, auditory, teacher, type, subgroup));
		}

		return lessons;
	}

	private String extractTime(String timeText) {
		Matcher m = timePattern.matcher(timeText);
		if (m.matches()) {
			String weekday = m.group(1);
			if (weekday != null) currentWeekday = weekday;

			return m.group(2);
		}
		throw new ParsingException("Can't parse time string: " + timeText);
	}

	private Parity extractParity(String parityText) {
		switch (parityText) {
			case oddParityString:
				return Parity.ODD;
			case evenParityString:
				return Parity.EVEN;
		}
		throw new ParsingException("Unknown parity: " + parityText);
	}

	private Lesson.Type extractType(String typeText) {
		switch (typeText) {
			case practiceString:
				return Lesson.Type.PRACTICE;
			case lectureString:
				return Lesson.Type.LECTURE;
			case laboratoryString:
				return Lesson.Type.LABORATORY;
		}
		return Lesson.Type.UNKNOWN;
	}

}
