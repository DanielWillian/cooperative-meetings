package votes;

import com.intuit.karate.junit5.Karate;

class VotesTest {
    @Karate.Test
    Karate testVotes() {
        return Karate.run("votes").relativeTo(getClass());
    }
}
