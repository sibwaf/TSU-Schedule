package ru.dyatel.tsuschedule.parsing;

import org.junit.Test;
import ru.dyatel.tsuschedule.util.Filter;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class FilterTest {

    @Test
    public void testParityFilterEven() {
        Filter<Lesson> filter = new ParityFilter(Parity.EVEN);
        assertTrue(filter.accept(new Lesson(Parity.EVEN, "", "", "", "", "", Lesson.Type.LABORATORY, 0)));
        assertTrue(filter.accept(new Lesson(Parity.BOTH, "", "", "", "", "", Lesson.Type.LABORATORY, 0)));
        assertFalse(filter.accept(new Lesson(Parity.ODD, "", "", "", "", "", Lesson.Type.LABORATORY, 0)));
    }

    @Test
    public void testParityFilterOdd() {
        Filter<Lesson> filter = new ParityFilter(Parity.ODD);
        assertTrue(filter.accept(new Lesson(Parity.ODD, "", "", "", "", "", Lesson.Type.LABORATORY, 0)));
        assertTrue(filter.accept(new Lesson(Parity.BOTH, "", "", "", "", "", Lesson.Type.LABORATORY, 0)));
        assertFalse(filter.accept(new Lesson(Parity.EVEN, "", "", "", "", "", Lesson.Type.LABORATORY, 0)));
    }

    @Test
    public void testParityFilterBoth() {
        Filter<Lesson> filter = new ParityFilter(Parity.BOTH);
        assertTrue(filter.accept(new Lesson(Parity.BOTH, "", "", "", "", "", Lesson.Type.LABORATORY, 0)));
        assertFalse(filter.accept(new Lesson(Parity.EVEN, "", "", "", "", "", Lesson.Type.LABORATORY, 0)));
        assertFalse(filter.accept(new Lesson(Parity.ODD, "", "", "", "", "", Lesson.Type.LABORATORY, 0)));
    }

    @Test
    public void testSubgroupFilterFirst() {
        Filter<Lesson> filter = new SubgroupFilter(1);
        assertTrue(filter.accept(new Lesson(Parity.ODD, "", "", "", "", "", Lesson.Type.LABORATORY, 0)));
        assertTrue(filter.accept(new Lesson(Parity.ODD, "", "", "", "", "", Lesson.Type.LABORATORY, 1)));
        assertFalse(filter.accept(new Lesson(Parity.ODD, "", "", "", "", "", Lesson.Type.LABORATORY, 2)));
    }

    @Test
    public void testSubgroupFilterSecond() {
        Filter<Lesson> filter = new SubgroupFilter(2);
        assertTrue(filter.accept(new Lesson(Parity.ODD, "", "", "", "", "", Lesson.Type.LABORATORY, 0)));
        assertFalse(filter.accept(new Lesson(Parity.ODD, "", "", "", "", "", Lesson.Type.LABORATORY, 1)));
        assertTrue(filter.accept(new Lesson(Parity.ODD, "", "", "", "", "", Lesson.Type.LABORATORY, 2)));
    }

    @Test
    public void testSubgroupFilterBoth() {
        Filter<Lesson> filter = new SubgroupFilter(0);
        assertTrue(filter.accept(new Lesson(Parity.ODD, "", "", "", "", "", Lesson.Type.LABORATORY, 0)));
        assertFalse(filter.accept(new Lesson(Parity.ODD, "", "", "", "", "", Lesson.Type.LABORATORY, 1)));
        assertFalse(filter.accept(new Lesson(Parity.ODD, "", "", "", "", "", Lesson.Type.LABORATORY, 2)));
    }

}
