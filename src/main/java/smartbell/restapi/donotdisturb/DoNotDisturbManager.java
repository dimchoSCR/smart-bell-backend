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

    private void updateDoNotDisturbStatus(long startTimeMillis, long endTimeMillis, boolean endTomorrow) {
        BellStatus.DoNotDisturbStatus doNotDisturbStatus = bellStatus.getDoNotDisturbStatus();
        doNotDisturbStatus.setStartTimeMillis(startTimeMillis);
        doNotDisturbStatus.setEndTimeMillis(endTimeMillis);
        doNotDisturbStatus.setEndTomorrow(endTomorrow);

        // TODO save to db
    }

    private void rescheduleDoNotDisturbStart(LocalDateTime startDateTime) {
        long initialDelayForJobStart = Duration.between(LocalDateTime.now(), startDateTime).getSeconds();
        log.info("Delay before start: " + initialDelayForJobStart);

        jobManager.cancelJobById(ENABLE_DO_NOT_DISTURB_REQUEST_ID, true);
        if (initialDelayForJobStart > 0) {
            disableDoNotDisturbMode();
            scheduleDoNotDisturbStartJob(initialDelayForJobStart);
        } else {
            enableDoNotDisturbMode();
            LocalDateTime newStartDateTime = startDateTime.plusDays(1);
            scheduleDoNotDisturbStartJob(Duration.between(LocalDateTime.now(), newStartDateTime).getSeconds());
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
        long initialDelayForJobEnd = Duration.between(LocalDateTime.now(), endDateTime).getSeconds();
        log.info("Delay before end: " + initialDelayForJobEnd);

        jobManager.cancelJobById(DISABLE_DO_NOT_DISTURB_REQUEST_ID, true);
        if (initialDelayForJobEnd > 0) {
            scheduleDoNotDisturbEndJob(initialDelayForJobEnd);
        } else {
            disableDoNotDisturbMode();
            LocalDateTime newEndDateTime = endDateTime.plusDays(1);
            scheduleDoNotDisturbEndJob(Duration.between(LocalDateTime.now(), newEndDateTime).getSeconds());
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

    public void scheduleDoNotDisturb(long startTimeMillis, long endTimeMillis, boolean endTomorrow) {
        try {
            updateDoNotDisturbStatus(startTimeMillis, endTimeMillis, endTomorrow);

            LocalDateTime startDateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(startTimeMillis), ZoneId.systemDefault());
            LocalDateTime endDateTime;

            if (endTomorrow) {
                endDateTime = LocalDateTime.ofInstant(
                        Instant.ofEpochMilli(endTimeMillis),
                        ZoneId.systemDefault()
                ).plusDays(1);
            } else {
                endDateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(endTimeMillis), ZoneId.systemDefault());
            }

            rescheduleDoNotDisturbStart(startDateTime);
            rescheduleDoNotDisturbEnd(endDateTime);
        } catch (DateTimeException err) {
            log.error("Could not parse start and end time millis for do not disturb!", err);
        }
    }
}
