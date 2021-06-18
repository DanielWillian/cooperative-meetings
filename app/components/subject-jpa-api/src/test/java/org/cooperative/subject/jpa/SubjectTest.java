package org.cooperative.subject.jpa;

import org.junit.Assert;
import org.junit.Test;

public class SubjectTest {

    @Test
    public void createBuilder() {
        Subject subject = Subject.builder()
                .id(1L)
                .name("name")
                .build();
        Assert.assertEquals(Long.valueOf(1L), subject.getId());
        Assert.assertEquals("name", subject.getName());
    }

    @Test
    public void equalsTrueTest() {
        Assert.assertEquals(Subject.of(1L, "name"), Subject.of(1L, "name"));
    }

    @Test
    public void equalsFalseTest() {
        Assert.assertNotEquals(Subject.of(1L, "name"), Subject.of(2L, "name"));
    }

    @Test
    public void hashCodeEqualsTest() {
        Assert.assertEquals(Subject.of(1L, "name").hashCode(), Subject.of(1L, "name").hashCode());
    }
}
