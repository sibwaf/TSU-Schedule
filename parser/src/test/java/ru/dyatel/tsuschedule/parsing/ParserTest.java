package ru.dyatel.tsuschedule.parsing;

import org.junit.BeforeClass;
import org.junit.Test;
import ru.dyatel.tsuschedule.util.Filter;
import ru.dyatel.tsuschedule.util.IterableFilter;

import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ParserTest {

    private Filter<Lesson> firstSubgroup = new SubgroupFilter(1);
    private Filter<Lesson> secondSubgroup = new SubgroupFilter(2);

    private static Set<Lesson> lessons;

    @BeforeClass
    public static void setup() throws Exception {
        lessons = Parser.getLessons("221251");
    }

    @Test
    public void testGetLessonsMondayOddFirst() throws Exception {
        IterableFilter<Lesson> filter = new IterableFilter<>();
        filter.apply(ParityFiltersKt.getOddParityFilter());
        filter.apply(new Filter<Lesson>() {
            @Override
            public boolean accept(Lesson obj) {
                return obj.getWeekday().equals("понедельник");
            }
        });
        filter.apply(firstSubgroup);

        Set<Lesson> result = filter.filter(lessons);
        assertEquals(4, result.size());
        assertTrue(containsDiscipline(result, "Лаб:Языки и методы программирования"));
        assertTrue(containsDiscipline(result, "Л:Алгебра и аналитическая геометрия"));
        assertTrue(containsDiscipline(result, "Пр:Алгебра и аналитическая геометрия"));
        removeDiscipline(result, "Пр:Алгебра и аналитическая геометрия");
        assertEquals(2, result.size());
    }

    @Test
    public void testGetLessonsMondayEvenFirst() throws Exception {
        IterableFilter<Lesson> filter = new IterableFilter<>();
        filter.apply(ParityFiltersKt.getEvenParityFilter());
        filter.apply(new Filter<Lesson>() {
            @Override
            public boolean accept(Lesson obj) {
                return obj.getWeekday().equals("понедельник");
            }
        });
        filter.apply(firstSubgroup);

        Set<Lesson> result = filter.filter(lessons);
        assertEquals(3, result.size());
        assertTrue(containsDiscipline(result, "Л:Языки и методы программирования"));
        assertTrue(containsDiscipline(result, "Л:Алгебра и аналитическая геометрия"));
        assertTrue(containsDiscipline(result, "Пр:Алгебра и аналитическая геометрия"));
    }

    @Test
    public void testGetLessonsMondayOddSecond() throws Exception {
        IterableFilter<Lesson> filter = new IterableFilter<>();
        filter.apply(ParityFiltersKt.getOddParityFilter());
        filter.apply(new Filter<Lesson>() {
            @Override
            public boolean accept(Lesson obj) {
                return obj.getWeekday().equals("понедельник");
            }
        });
        filter.apply(secondSubgroup);

        Set<Lesson> result = filter.filter(lessons);
        assertEquals(2, result.size());
        assertTrue(containsDiscipline(result, "Лаб:Языки и методы программирования"));
        assertTrue(containsDiscipline(result, "Л:Алгебра и аналитическая геометрия"));
    }

    @Test
    public void testGetLessonsFriday() throws Exception {
        IterableFilter<Lesson> filter = new IterableFilter<>();
        filter.apply(ParityFiltersKt.getOddParityFilter());
        filter.apply(new Filter<Lesson>() {
            @Override
            public boolean accept(Lesson obj) {
                return obj.getWeekday().equals("пятница");
            }
        });
        filter.apply(firstSubgroup);

        Set<Lesson> result = filter.filter(lessons);
        assertEquals(2, result.size());
        assertTrue(containsDiscipline(result, "Л:Математический анализ"));
        assertTrue(containsDiscipline(result, "Пр:Физическая культура (элективный курс)"));
    }

    private static boolean containsDiscipline(Set<Lesson> lessons, String discipline) {
        for (Lesson l : lessons) {
            if (l.getDiscipline().equals(discipline)) return true;
        }
        return false;
    }

    private static void removeDiscipline(Set<Lesson> lessons, String discipline) {
        while (true) {
            Lesson lesson = null;
            for (Lesson l : lessons) {
                if (l.getDiscipline().equals(discipline)) {
                    lesson = l;
                    break;
                }
            }
            if (lesson == null) break;
            else lessons.remove(lesson);
        }
    }

}