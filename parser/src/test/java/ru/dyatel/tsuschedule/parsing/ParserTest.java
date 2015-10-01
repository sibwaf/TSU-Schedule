package ru.dyatel.tsuschedule.parsing;

import org.junit.BeforeClass;
import org.junit.Test;
import ru.dyatel.tsuschedule.parsing.util.Filter;
import ru.dyatel.tsuschedule.parsing.util.IterableFilter;

import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ParserTest {

    private Filter<Lesson> oddParity = new Filter<Lesson>() {
        @Override
        public boolean accept(Lesson obj) {
            return obj.getParity().equals(Parity.ODD) || obj.getParity().equals(Parity.BOTH);
        }
    };
    private Filter<Lesson> firstSubgroup = new Filter<Lesson>() {
        @Override
        public boolean accept(Lesson obj) {
            return obj.getSubgroup() == 0 || obj.getSubgroup() == 1;
        }
    };

    private static Set<Lesson> lessons;

    @BeforeClass
    public static void setup() throws Exception {
        lessons = Parser.getLessons("221251");
    }

    @Test
    public void testGetLessonsMonday() throws Exception {
        IterableFilter<Lesson> filter = new IterableFilter<Lesson>();
        filter.apply(oddParity);
        filter.apply(new Filter<Lesson>() {
            @Override
            public boolean accept(Lesson obj) {
                return obj.getWeekday().equals("понедельник");
            }
        });
        filter.apply(firstSubgroup);

        Set<Lesson> result = filter.filter(lessons);
        assertEquals(3, result.size());
        assertTrue(containsDiscipline(result, "Пр:История"));
        assertTrue(containsDiscipline(result, "Л:История"));
        assertTrue(containsDiscipline(result, "Пр:Иностранный язык"));
    }

    @Test
    public void testGetLessonsTuesday() throws Exception {
        IterableFilter<Lesson> filter = new IterableFilter<Lesson>();
        filter.apply(oddParity);
        filter.apply(new Filter<Lesson>() {
            @Override
            public boolean accept(Lesson obj) {
                return obj.getWeekday().equals("вторник");
            }
        });
        filter.apply(firstSubgroup);

        Set<Lesson> result = filter.filter(lessons);
        assertEquals(2, result.size());
        assertTrue(containsDiscipline(result, "Л:Физическая культура"));
        assertTrue(containsDiscipline(result, "Л:Алгебра и аналитическая геометрия"));
    }

    private boolean containsDiscipline(Set<Lesson> lessons, String discipline) {
        for (Lesson l : lessons) {
            if (l.getDiscipline().equals(discipline)) return true;
        }
        return false;
    }

}