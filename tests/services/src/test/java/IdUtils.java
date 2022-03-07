import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

public class IdUtils {
    private static final long BILLION = 1000000000;
    private static final long MILLION = 1000000;
    private static final long THOUSAND = 1000;

    public static long randomLong() {
        long random = ThreadLocalRandom.current().nextLong(0, BILLION);
        return Long.parseLong("" +
                (System.currentTimeMillis() / THOUSAND) +
                String.format("%09d", random));
    }

    public static String randomString() {
        long random = ThreadLocalRandom.current().nextLong(0, MILLION);
        return String.format("%06d", random);
    }

    public static String randomUuid() {
        return UUID.randomUUID().toString();
    }
}
