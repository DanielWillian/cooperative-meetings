package polls;

import com.intuit.karate.junit5.Karate;

class PollsTest {

    @Karate.Test
    Karate testPolls() {
        return Karate.run("polls").relativeTo(getClass());
    }
}
