package ru.dyatel.tsuschedule.parsing;

import org.junit.Test;

public class ParserWrongDataTest {

    @Test(expected = IllegalArgumentException.class)
    public void testWrongGroup() throws Exception {
        new Parser().getLessons("221");
    }

}
