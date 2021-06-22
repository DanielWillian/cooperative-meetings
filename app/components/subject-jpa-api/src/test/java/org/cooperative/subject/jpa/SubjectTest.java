package org.cooperative.subject.jpa;


import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

public class SubjectTest {

    @Test
    public void createBuilder() {
        Subject subject = Subject.builder()
                .id(1L)
                .name("name")
                .build();
        assertEquals(Long.valueOf(1L), subject.getId());
        assertEquals("name", subject.getName());
    }

    @Test
    public void equalsTrueTest() {
        assertEquals(Subject.of(1L, "name"), Subject.of(1L, "name"));
    }

    @Test
    public void equalsFalseTest() {
        assertNotEquals(Subject.of(1L, "name"), Subject.of(2L, "name"));
    }

    @Test
    public void hashCodeEqualsTest() {
        assertEquals(Subject.of(1L, "name").hashCode(), Subject.of(1L, "name").hashCode());
    }
}
