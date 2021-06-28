package org.cooperative.poll.api;

import lombok.extern.slf4j.Slf4j;
import org.cooperative.poll.Poll;
import org.cooperative.poll.PollService;
import org.cooperative.poll.api.model.PollCreate;
import org.cooperative.poll.api.model.PollResponse;
import org.cooperative.poll.api.model.PollUpdate;
import org.cooperative.poll.exception.PollNotFoundException;
import org.cooperative.poll.exception.PollValidationException;
import org.cooperative.subject.SubjectNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Component
@Slf4j
public class PollsApiDefault implements PollsApiDelegate {

    private PollService pollService;

    @Autowired
    public PollsApiDefault(PollService pollService) {
        this.pollService = pollService;
    }

    @Override
    public ResponseEntity<PollResponse> addPoll(Long subjectId, PollCreate pollCreate) {
        try {
            log.trace("ENTRY - subjectId: {}, pollCreate: {}", subjectId, pollCreate);
            Poll poll = Poll.builder()
                    .name(pollCreate.getName())
                    .startDate(OffsetDateTime.now())
                    .endDate(pollCreate.getEndDate())
                    .subjectId(subjectId)
                    .build();
            Poll createdPoll = pollService.createPoll(poll);
            ResponseEntity<PollResponse> response = ResponseEntity.created(
                    URI.create("/subjects/" + subjectId + "/polls/" + createdPoll.getId()))
                    .body(mapToResponse(createdPoll));
            log.trace("EXIT");
            return response;
        } catch (PollValidationException | SubjectNotFoundException e) {
            throw PollsApiException.builder()
                    .throwable(e)
                    .status(HttpStatus.BAD_REQUEST)
                    .build();
        } catch (Exception e) {
            log.error("Could not add Poll - subjectId: " + subjectId + ", pollCreate: " + pollCreate, e);
            throw e;
        }
    }

    @Override
    public ResponseEntity<Void> deletePollById(Long subjectId, Long pollId) {
        try {
            log.trace("ENTRY - subjectId: {}, pollId: {}", subjectId, pollId);
            pollService.deletePollByIdAndSubjectId(pollId, subjectId);
            ResponseEntity<Void> response = ResponseEntity.noContent().build();
            log.trace("EXIT");
            return response;
        } catch (PollNotFoundException | SubjectNotFoundException e) {
            throw PollsApiException.builder()
                    .throwable(e)
                    .status(HttpStatus.NOT_FOUND)
                    .build();
        } catch (Exception e) {
            log.error("Could not add Poll - subjectId: " + subjectId, e);
            throw e;
        }
    }

    @Override
    public ResponseEntity<PollResponse> getPollById(Long subjectId, Long pollId) {
        try {
            log.trace("ENTRY - subjectId: {}, pollId: {}", subjectId, pollId);
            PollResponse poll = pollService.getPollByIdAndSubjectId(pollId, subjectId)
                    .map(this::mapToResponse)
                    .orElseThrow(PollNotFoundException::new);
            ResponseEntity<PollResponse> response = ResponseEntity.ok(poll);
            log.trace("EXIT");
            return response;
        } catch (SubjectNotFoundException | PollNotFoundException e) {
            throw PollsApiException.builder()
                    .throwable(e)
                    .status(HttpStatus.NOT_FOUND)
                    .build();
        } catch (Exception e) {
            log.error("Could not add Poll - subjectId: " + subjectId, e);
            throw e;
        }
    }

    @Override
    public ResponseEntity<List<PollResponse>> getPolls(Long subjectId) {
        try {
            log.trace("ENTRY - subjectId: {}", subjectId);
            List<PollResponse> pollResponses = pollService.getPollBySubjectId(subjectId)
                    .map(this::mapToResponse)
                    .collect(Collectors.toList());
            ResponseEntity<List<PollResponse>> response = ResponseEntity.ok(pollResponses);
            log.trace("EXIT");
            return response;
        } catch (Exception e) {
            log.error("Could not get Polls - subjectId: " + subjectId, e);
            throw e;
        }
    }

    @Override
    public ResponseEntity<PollResponse> updatePoll(Long subjectId, PollUpdate pollUpdate) {
        try {
            log.trace("ENTRY - subjectId: {}, pollUpdate: {}", subjectId, pollUpdate);
            Poll currentPoll = pollService.getPollByIdAndSubjectId(subjectId, pollUpdate.getId())
                    .orElseThrow(PollNotFoundException::new);

            Poll.PollBuilder builder = Poll.builder()
                    .subjectId(subjectId)
                    .id(pollUpdate.getId())
                    .endDate(pollUpdate.getEndDate());
            if (pollUpdate.getName() != null) builder.name(pollUpdate.getName());
            else builder.name(currentPoll.getName());

            Poll updatedPoll = pollService.updatePoll(builder.build());
            ResponseEntity<PollResponse> response = ResponseEntity.ok(mapToResponse(updatedPoll));
            log.trace("EXIT");
            return response;
        } catch (PollValidationException e) {
            throw PollsApiException.builder()
                    .throwable(e)
                    .status(HttpStatus.BAD_REQUEST)
                    .build();
        } catch (PollNotFoundException | SubjectNotFoundException e) {
            throw PollsApiException.builder()
                    .throwable(e)
                    .status(HttpStatus.NOT_FOUND)
                    .build();
        }catch (Exception e) {
            log.error("Could not update Poll - subjectId: " + subjectId + ", pollUpdate: " + pollUpdate, e);
            throw e;
        }
    }

    private PollResponse mapToResponse(Poll poll) {
        PollResponse pollResponse = new PollResponse();
        pollResponse.setId(poll.getId());
        pollResponse.setName(poll.getName());
        pollResponse.setEndDate(poll.getEndDate());
        return pollResponse;
    }
}
