package subjects;

import com.intuit.karate.junit5.Karate;

class SubjectsTest {

    @Karate.Test
    Karate testSubjects() {
        return Karate.run("subjects").relativeTo(getClass());
    }
}
