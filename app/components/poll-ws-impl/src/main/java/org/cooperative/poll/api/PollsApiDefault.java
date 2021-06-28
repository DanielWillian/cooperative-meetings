package org.cooperative.poll.api;

import org.cooperative.poll.PollService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class PollsApiDefault implements PollsApiDelegate {

    private PollService pollService;

    @Autowired
    public PollsApiDefault(PollService pollService) {
        this.pollService = pollService;
    }
}
