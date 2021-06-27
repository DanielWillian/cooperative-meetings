package org.cooperative.subject.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.cooperative.subject.StubSubjectService;
import org.cooperative.subject.SubjectService;
import org.cooperative.subject.api.model.Subject;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.net.URI;
import java.util.Optional;

import static org.hamcrest.Matchers.emptyString;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.collection.IsIterableContainingInAnyOrder.containsInAnyOrder;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(SubjectsApiController.class)
public class SubjectsApiTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper mapper;

    @Autowired
    private SubjectsApiDelegate subjectsApiDelegate;

    @Autowired
    private StubSubjectService service;

    @Configuration
    public static class TestConfig {
        @Bean
        public SubjectsApi subjectsApiController(SubjectsApiDelegate delegate) {
            return new SubjectsApiController(delegate);
        }

        @Bean
        public SubjectsApiDelegate subjectsApiDelegate(SubjectService service) {
            return new SubjectsApiDefault(service);
        }

        @Bean
        public SubjectService service() {
            return new StubSubjectService();
        }

        @Bean
        public SubjectsApiErrorHandler subjectsApiErrorHandler() {
            return new SubjectsApiErrorHandler();
        }
    }

    @AfterEach
    public void afterTest() {
        service.deleteAllSubjects();
    }

    @Test
    public void testGetSubjectsNoSubjects() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders
                .get(URI.create("/subjects"))
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    public void testGetSubjectsSomeSubjects() throws Exception {
        service.createSubject(org.cooperative.subject.Subject.of(0, "name0"));
        service.createSubject(org.cooperative.subject.Subject.of(1, "name1"));

        mockMvc.perform(MockMvcRequestBuilders
                .get(URI.create("/subjects"))
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].id", containsInAnyOrder(0, 1)))
                .andExpect(jsonPath("$[*].name", containsInAnyOrder("name0", "name1")));
    }

    @Test
    public void testAddSubjectSuccess() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders
                .post(URI.create("/subjects"))
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(createSubject(0, "name"))))
                .andExpect(status().isCreated())
                .andExpect(header().string(HttpHeaders.LOCATION, "/subjects/0"))
                .andExpect(jsonPath("$.id", is(0)))
                .andExpect(jsonPath("$.name", is("name")));

        Optional<org.cooperative.subject.Subject> subject = service.getSubjectById(0);
        assertTrue(subject.isPresent());
        assertEquals(org.cooperative.subject.Subject.of(0, "name"), subject.get());
    }

    @Test
    public void testAddSubjectWrongFormat() throws Exception {
        testWrongFormatRequest(MockMvcRequestBuilders
                .post(URI.create("/subjects")));
    }

    @Test
    public void testAddSubjectAlreadyExists() throws Exception {
        service.createSubject(org.cooperative.subject.Subject.of(0, "name"));

        mockMvc.perform(MockMvcRequestBuilders
                .post(URI.create("/subjects"))
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(createSubject(0, "name"))))
                .andExpect(status().isConflict());
    }

    @Test
    public void testUpdateSubjectSuccess() throws Exception {
        service.createSubject(org.cooperative.subject.Subject.of(0, "name"));

        mockMvc.perform(MockMvcRequestBuilders
                .put(URI.create("/subjects"))
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(createSubject(0, "updated"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(0)))
                .andExpect(jsonPath("$.name", is("updated")));

        Optional<org.cooperative.subject.Subject> subject = service.getSubjectById(0);
        assertTrue(subject.isPresent());
        assertEquals(org.cooperative.subject.Subject.of(0, "updated"), subject.get());
    }

    @Test
    public void testUpdateSubjectWrongFormat() throws Exception {
        testWrongFormatRequest(MockMvcRequestBuilders
                .put(URI.create("/subjects")));
    }

    @Test
    public void testUpdateSubjectNotFound() throws Exception {
        testNotFoundRequest(MockMvcRequestBuilders
                .put(URI.create("/subjects"))
                .content(mapper.writeValueAsString(createSubject(0, "name"))));
    }

    @Test
    public void testGetSubjectSuccess() throws Exception {
        service.createSubject(org.cooperative.subject.Subject.of(0, "name"));

        mockMvc.perform(MockMvcRequestBuilders
                .get(URI.create("/subjects/0"))
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(0)))
                .andExpect(jsonPath("$.name", is("name")));
    }

    @Test
    public void testGetSubjectWrongFormat() throws Exception {
        testWrongFormatRequest(MockMvcRequestBuilders
                .get(URI.create("/subjects/-1")));
    }

    @Test
    public void testGetSubjectNotFound() throws Exception {
        testNotFoundRequest(MockMvcRequestBuilders
                .get(URI.create("/subjects/0")));
    }

    @Test
    public void testDeleteSubjectSuccess() throws Exception {
        service.createSubject(org.cooperative.subject.Subject.of(0, "name"));

        mockMvc.perform(MockMvcRequestBuilders
                .delete(URI.create("/subjects/0"))
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());
    }

    @Test
    public void testDeleteSubjectWrongFormat() throws Exception {
        testWrongFormatRequest(MockMvcRequestBuilders
                .delete(URI.create("/subjects/-1")));
    }

    @Test
    public void testDeleteSubjectNotFound() throws Exception {
        testNotFoundRequest(MockMvcRequestBuilders
                .delete(URI.create("/subjects/0")));
    }

    private void testWrongFormatRequest(MockHttpServletRequestBuilder request)
            throws Exception {
        mockMvc.perform(request
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(createSubject(-1, "name"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", not(emptyString())));
    }

    private void testNotFoundRequest(MockHttpServletRequestBuilder request)
            throws Exception {
        mockMvc.perform(request
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    private Subject createSubject(long id, String name) {
        Subject subject = new Subject();
        subject.setId(id);
        subject.setName(name);
        return subject;
    }
}
