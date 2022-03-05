package org.cooperative.poll.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.cooperative.poll.Poll;
import org.cooperative.poll.PollService;
import org.cooperative.poll.PollServiceDefault;
import org.cooperative.poll.api.model.PollCreate;
import org.cooperative.poll.api.model.PollResponse;
import org.cooperative.poll.api.model.PollUpdate;
import org.cooperative.poll.jpa.PollRepository;
import org.cooperative.poll.jpa.StubPollRepository;
import org.cooperative.subject.StubSubjectService;
import org.cooperative.subject.Subject;
import org.cooperative.subject.SubjectService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.net.URI;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.Optional;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.collection.IsIterableContainingInAnyOrder.containsInAnyOrder;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PollsApiController.class)
public class PollsApiTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper mapper;

    @Autowired
    private PollsApiDelegate pollsApiDelegate;

    @Autowired
    private PollService service;

    @Autowired
    private StubPollRepository stubPollRepository;

    @Autowired
    private StubSubjectService stubSubjectService;

    private final String startTimeString = "2021-06-27T12:00:00Z";
    private final String endTimeString = "2021-06-27T13:00:00Z";
    private final OffsetDateTime startTime = OffsetDateTime.parse(startTimeString);
    private final OffsetDateTime endTime = OffsetDateTime.parse(endTimeString);

    @Configuration
    public static class TestConfig {
        @Bean
        public PollsApi pollsApiController(PollsApiDelegate delegate) {
            return new PollsApiController(delegate);
        }

        @Bean
        public PollsApiDelegate pollsApiDelegate(PollService service) {
            return new PollsApiDefault(service);
        }

        @Bean
        public PollService service(PollRepository repository, SubjectService service) {
            return new PollServiceDefault(repository, service);
        }

        @Bean
        public PollRepository pollRepository() {
            return new StubPollRepository();
        }

        @Bean
        public SubjectService subjectService() {
            return new StubSubjectService();
        }

        @Bean
        public PollsApiErrorHandler pollsApiErrorHandler() {
            return new PollsApiErrorHandler();
        }
    }

    @AfterEach
    public void afterTest() {
        stubPollRepository.deleteAll();
        stubSubjectService.deleteAllSubjects();
    }

    @Test
    public void testGetPollsNoPolls() throws Exception {
        stubSubjectService.createSubject(Subject.of(1L, "subject"));

        mockMvc.perform(MockMvcRequestBuilders
                .get(URI.create("/subjects/1/polls"))
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    public void testGetPollsSomePolls() throws Exception {
        stubSubjectService.createSubject(Subject.of(1L, "subject"));
        service.createPoll(Poll.of(1L, "poll1", startTime, endTime, 1L));
        service.createPoll(Poll.of(2L, "poll2", startTime, endTime, 1L));

        mockMvc.perform(MockMvcRequestBuilders
                .get(URI.create("/subjects/1/polls"))
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].id", containsInAnyOrder(1, 2)))
                .andExpect(jsonPath("$[*].name", containsInAnyOrder("poll1", "poll2")))
                .andExpect(jsonPath("$[*].endDate",
                        containsInAnyOrder(endTimeString, endTimeString)));
    }

    @Test
    public void testAddPollSuccess() throws Exception {
        stubSubjectService.createSubject(Subject.of(1L, "subject"));

        OffsetDateTime endDate = OffsetDateTime.now(ZoneId.of("UTC")).plus(Duration.ofHours(1));

        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders
                .post(URI.create("/subjects/1/polls"))
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(createPollCreate("poll", endDate))))
                .andExpect(status().isCreated())
                .andExpect(header().string(HttpHeaders.LOCATION, "/subjects/1/polls/1"))
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.name", is("poll")))
                .andReturn();
        PollResponse pollResponse = mapper.readValue(mvcResult.getResponse().getContentAsString(),
                PollResponse.class);
        assertEquals(endDate, pollResponse.getEndDate());

        Optional<Poll> optional = service.getPollByIdAndSubjectId(1L, 1L);
        assertTrue(optional.isPresent());
        Poll poll = optional.get();
        assertEquals(1L, poll.getId());
        assertEquals("poll", poll.getName());
        assertEquals(endDate, poll.getEndDate());
    }

    @Test
    public void testAddPollNoEndDate() throws Exception {
        stubSubjectService.createSubject(Subject.of(1L, "subject"));

        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders
                .post(URI.create("/subjects/1/polls"))
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(createPollCreate("poll", null))))
                .andExpect(status().isCreated())
                .andExpect(header().string(HttpHeaders.LOCATION, "/subjects/1/polls/1"))
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.name", is("poll")))
                .andReturn();
        PollResponse pollResponse = mapper.readValue(mvcResult.getResponse().getContentAsString(),
                PollResponse.class);
        assertFalse(pollResponse.getEndDate().isAfter(
                OffsetDateTime.now().plus(Duration.ofMinutes(1))));

        Optional<Poll> optional = service.getPollByIdAndSubjectId(1L, 1L);
        assertTrue(optional.isPresent());
        Poll poll = optional.get();
        assertEquals(1L, poll.getId());
        assertEquals("poll", poll.getName());
        assertFalse(poll.getEndDate().isAfter(OffsetDateTime.now().plus(Duration.ofMinutes(1))));
    }

    @Test
    public void testAddPollWrongFormat() throws Exception {
        stubSubjectService.createSubject(Subject.of(1L, "subject"));

        OffsetDateTime endDate = OffsetDateTime.now(ZoneId.of("UTC")).minus(Duration.ofHours(1));

        mockMvc.perform(MockMvcRequestBuilders
                .post(URI.create("/subjects/1/polls"))
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(createPollCreate("poll", endDate))))
                .andExpect(status().isBadRequest());
    }

    private PollCreate createPollCreate(String name, OffsetDateTime endDate) {
        PollCreate pollCreate = new PollCreate();
        pollCreate.setName(name);
        pollCreate.setEndDate(endDate);
        return pollCreate;
    }

    @Test
    public void testUpdatePollSuccess() throws Exception {
        stubSubjectService.createSubject(Subject.of(1L, "subject"));
        OffsetDateTime endDate = OffsetDateTime.now(ZoneId.of("UTC")).plus(Duration.ofHours(1));

        service.createPoll(Poll.of(1L, "poll", startTime, endDate, 1L));

        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders
                .put(URI.create("/subjects/1/polls"))
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(createPollUpdate(1L, "poll updated", endDate))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.name", is("poll updated")))
                .andReturn();
        PollResponse pollResponse = mapper.readValue(mvcResult.getResponse().getContentAsString(),
                PollResponse.class);
        assertEquals(endDate, pollResponse.getEndDate());

        Optional<Poll> optional = service.getPollByIdAndSubjectId(1L, 1L);
        assertTrue(optional.isPresent());
        Poll poll = optional.get();
        assertEquals(1L, poll.getId());
        assertEquals("poll updated", poll.getName());
        assertEquals(endDate, poll.getEndDate());
    }

    @Test
    public void testUpdatePollWrongFormat() throws Exception {
        stubSubjectService.createSubject(Subject.of(1L, "subject"));
        OffsetDateTime endDate = OffsetDateTime.now(ZoneId.of("UTC")).minus(Duration.ofHours(1));

        service.createPoll(Poll.of(1L, "poll", startTime, endDate, 1L));

        mockMvc.perform(MockMvcRequestBuilders
                .put(URI.create("/subjects/1/polls"))
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(createPollUpdate(
                        1L, "poll", startTime.minus(Duration.ofDays(1))))))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testUpdatePollNotFound() throws Exception {
        stubSubjectService.createSubject(Subject.of(1L, "subject"));
        OffsetDateTime endDate = OffsetDateTime.now(ZoneId.of("UTC")).plus(Duration.ofHours(1));

        mockMvc.perform(MockMvcRequestBuilders
                .put(URI.create("/subjects/1/polls"))
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(createPollUpdate(1L, "name", endDate))))
                .andExpect(status().isNotFound());
    }

    private PollUpdate createPollUpdate(Long id, String name, OffsetDateTime endDate) {
        PollUpdate pollCreate = new PollUpdate();
        pollCreate.setId(id);
        pollCreate.setName(name);
        pollCreate.setEndDate(endDate);
        return pollCreate;
    }

    @Test
    public void testGetPollSuccess() throws Exception {
        stubSubjectService.createSubject(Subject.of(1L, "subject"));
        service.createPoll(Poll.of(1L, "poll", startTime, endTime, 1L));

        mockMvc.perform(MockMvcRequestBuilders
                .get(URI.create("/subjects/1/polls/1"))
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.name", is("poll")))
                .andExpect(jsonPath("$.endDate", is(endTimeString)));
    }

    @Test
    public void testGetPollNotFound() throws Exception {
        stubSubjectService.createSubject(Subject.of(1L, "subject"));

        mockMvc.perform(MockMvcRequestBuilders
                .get(URI.create("/subjects/1/polls/1"))
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    public void testDeletePollSuccess() throws Exception {
        stubSubjectService.createSubject(Subject.of(1L, "subject"));
        service.createPoll(Poll.of(1L, "poll", startTime, endTime, 1L));

        mockMvc.perform(MockMvcRequestBuilders
                .delete(URI.create("/subjects/1/polls/1")))
                .andExpect(status().isNoContent());
    }

    @Test
    public void testDeletePollNotFound() throws Exception {
        stubSubjectService.createSubject(Subject.of(1L, "subject"));

        mockMvc.perform(MockMvcRequestBuilders
                .delete(URI.create("/subjects/1/polls/1")))
                .andExpect(status().isNotFound());
    }
}
