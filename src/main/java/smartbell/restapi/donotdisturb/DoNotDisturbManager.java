package smartbell.restapi.donotdisturb;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import smartbell.restapi.BellStatus;
import smartbell.restapi.job.JobManager;
import smartbell.restapi.job.JobRequest;

import java.time.*;
import java.util.concurrent.TimeUnit;

@Component
public class DoNotDisturbManager {

    private static final long PERIOD_24_HOURS_AS_SECONDS = 120; // 86400

    private static final String ENABLE_DO_NOT_DISTURB_REQUEST_ID = "StartDoNotDisturb";
    private static final String DISABLE_DO_NOT_DISTURB_REQUEST_ID = "EndDoNotDisturb";

    private final Logger log = LoggerFactory.getLogger(DoNotDisturbManager.class);

    @Autowired
    private BellStatus bellStatus;

    @Autowired
    private StartDoNotDisturbJob startDoNotDisturbJob;

    @Autowired
    private EndDoNotDisturbJob endDoNotDisturbJob;

    @Autowired
    private JobManager jobManager;

    private void updateDoNotDisturbStatus(String startTime, String endTime, boolean endTomorrow) {
        BellStatus.DoNotDisturbStatus doNotDisturbStatus = bellStatus.getDoNotDisturbStatus();
        doNotDisturbStatus.setStartTime(startTime);
        doNotDisturbStatus.setEndTime(endTime);
        doNotDisturbStatus.setEndTomorrow(endTomorrow);

        // TODO save to db
    }

    private void rescheduleDoNotDisturbStart(LocalDateTime startDateTime) {
        ZoneId zoneId = ZoneId.of("Europe/Sofia");
        long initialDelayForJobStart = Duration.between(LocalDateTime.now(zoneId), startDateTime).getSeconds();
        log.info("Delay before start: " + initialDelayForJobStart);

        jobManager.cancelJobById(ENABLE_DO_NOT_DISTURB_REQUEST_ID, true);
        if (initialDelayForJobStart > 0) {
            disableDoNotDisturbMode();
            scheduleDoNotDisturbStartJob(initialDelayForJobStart);
        } else {
            enableDoNotDisturbMode();
            LocalDateTime newStartDateTime = startDateTime.plusDays(1);
            scheduleDoNotDisturbStartJob(Duration.between(LocalDateTime.now(zoneId), newStartDateTime).getSeconds());
        }
    }

    private void scheduleDoNotDisturbStartJob(long initialDelayForStartJob) {

        JobRequest startDoNotDisturbRequest = new JobRequest.Builder(startDoNotDisturbJob)
                .setRequestId(ENABLE_DO_NOT_DISTURB_REQUEST_ID)
                .setStartDelay(initialDelayForStartJob)
                .setRecurringPeriod(PERIOD_24_HOURS_AS_SECONDS)
                .setTimeUnit(TimeUnit.SECONDS)
                .build();

        jobManager.schedule(startDoNotDisturbRequest);
    }

    private void rescheduleDoNotDisturbEnd(LocalDateTime endDateTime) {
        ZoneId zoneId = ZoneId.of("Europe/Sofia"); // TODO remove
        long initialDelayForJobEnd = Duration.between(LocalDateTime.now(zoneId), endDateTime).getSeconds();
        log.info("Delay before end: " + initialDelayForJobEnd);

        jobManager.cancelJobById(DISABLE_DO_NOT_DISTURB_REQUEST_ID, true);
        if (initialDelayForJobEnd > 0) {
            scheduleDoNotDisturbEndJob(initialDelayForJobEnd);
        } else {
            disableDoNotDisturbMode();
            LocalDateTime newEndDateTime = endDateTime.plusDays(1);
            scheduleDoNotDisturbEndJob(Duration.between(LocalDateTime.now(zoneId), newEndDateTime).getSeconds());
        }
    }

    private void scheduleDoNotDisturbEndJob(long initialDelayForEndJob) {
        JobRequest endDoNotDisturbRequest = new JobRequest.Builder(endDoNotDisturbJob)
                .setRequestId(DISABLE_DO_NOT_DISTURB_REQUEST_ID)
                .setStartDelay(initialDelayForEndJob)
                .setRecurringPeriod(PERIOD_24_HOURS_AS_SECONDS)
                .setTimeUnit(TimeUnit.SECONDS)
                .build();

        jobManager.schedule(endDoNotDisturbRequest);
    }

    public void enableDoNotDisturbMode() {
        bellStatus.getDoNotDisturbStatus().setInDoNotDisturb(true);
    }

    public void disableDoNotDisturbMode() {
        bellStatus.getDoNotDisturbStatus().setInDoNotDisturb(false);
    }

    public void scheduleDoNotDisturb(String startTime, String endTime, boolean endTomorrow) {
        updateDoNotDisturbStatus(startTime, endTime, endTomorrow);

        // LocalDateTime.ofInstant(Instant.ofEpochMilli(longValue), ZoneId.systemDefault());
        // TODO exception handling
        LocalDate now = LocalDate.now();
        LocalDateTime startDateTime = LocalTime.parse(startTime).atDate(now);
        LocalDateTime endDateTime;
        if (endTomorrow) {
            endDateTime = LocalTime.parse(endTime).atDate(now.plusDays(1));
        } else {
            endDateTime = LocalTime.parse(endTime).atDate(now);
        }

        rescheduleDoNotDisturbStart(startDateTime);
        rescheduleDoNotDisturbEnd(endDateTime);
    }
}
