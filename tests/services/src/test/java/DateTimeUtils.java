import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.ZoneId;

public class DateTimeUtils {
    private static final ZoneId UTC = ZoneId.of("UTC");

    public static String getFutureOffsetDateTimeString() {
        return OffsetDateTime.now(UTC)
                .plus(Duration.ofHours(1))
                .toString();
    }

    public static String getRecentFutureOffsetDateTimeString() {
        return OffsetDateTime.now(UTC)
                .plus(Duration.ofSeconds(1))
                .toString();
    }

    public static String getPastOffsetDateTimeString() {
        return OffsetDateTime.now(UTC)
                .minus(Duration.ofHours(1))
                .toString();
    }
}
