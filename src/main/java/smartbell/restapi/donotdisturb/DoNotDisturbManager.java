package smartbell.restapi.donotdisturb;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import smartbell.restapi.BellExecutorService;
import smartbell.restapi.status.BellStatus;
import smartbell.restapi.job.JobManager;
import smartbell.restapi.job.JobRequest;
import smartbell.restapi.melody.MelodyStorageProperties;
import smartbell.restapi.status.DoNotDisturbStatus;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.time.*;
import java.util.concurrent.TimeUnit;

@Component
public class DoNotDisturbManager {

    private static final String ENABLE_DO_NOT_DISTURB_REQUEST_ID = "StartDoNotDisturb";
    private static final String DISABLE_DO_NOT_DISTURB_REQUEST_ID = "EndDoNotDisturb";
    private static final String DO_NOT_DISTURB_CONFIG_FILE_NAME = "DoNotDisturbConf.json";

    private final Logger log = LoggerFactory.getLogger(DoNotDisturbManager.class);

    public static final String KEY_DAYS_ARRAY = "DaysArray";
    public static final String KEY_DISTURB_DURATION = "DisturbDuration";
    public static final long PERIOD_24_HOURS_AS_SECONDS = 86400;

    @Autowired
    private BellStatus bellStatus;

    @Autowired
    private ObjectMapper jacksonObjectMapper;

    @Autowired
    private MelodyStorageProperties melodyStorageProps;

    @Autowired
    private BellExecutorService bellExecutorService;

    @Autowired
    private StartDoNotDisturbJob startDoNotDisturbJob;

    @Autowired
    private EndDoNotDisturbJob endDoNotDisturbJob;

    @Autowired
    private JobManager jobManager;


    private String doNotDisturbConfPath;

    @PostConstruct
    private void init() {
         doNotDisturbConfPath = Paths.get(
                melodyStorageProps.getBaseDirPath(),
                DO_NOT_DISTURB_CONFIG_FILE_NAME
        ).toString();
    }

    private void updateDoNotDisturbStatus(int[] days, long startTimeMillis, long endTimeMillis, boolean endTomorrow) {
        DoNotDisturbStatus doNotDisturbStatus = bellStatus.getDoNotDisturbStatus();
        doNotDisturbStatus.setDays(days);
        doNotDisturbStatus.setStartTimeMillis(startTimeMillis);
        doNotDisturbStatus.setEndTimeMillis(endTimeMillis);
        doNotDisturbStatus.setEndTomorrow(endTomorrow);

        // Persist config async
        bellExecutorService.io.execute(() -> {
            try {
                jacksonObjectMapper.writeValue(new File(doNotDisturbConfPath), doNotDisturbStatus);
                log.info("Persisted DoNotDisturb conf");
            } catch (IOException e) {
                log.error("Error while saving do not disturb configuration", e);
            }
        });

    }

    private void rescheduleDoNotDisturbStart(LocalDateTime startDateTime) {
        long initialDelayForJobStart = Duration.between(LocalDateTime.now(), startDateTime).getSeconds();
        log.info("Delay before start: " + initialDelayForJobStart);

        jobManager.cancelJobById(ENABLE_DO_NOT_DISTURB_REQUEST_ID, true);
        if (initialDelayForJobStart >= 0) {
            disableDoNotDisturbMode();
            scheduleDoNotDisturbStartJob(initialDelayForJobStart);
        } else {
            enableDoNotDisturbMode();
            LocalDateTime newStartDateTime = startDateTime.plusSeconds(PERIOD_24_HOURS_AS_SECONDS);
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
        if (initialDelayForJobEnd >= 0) {
            scheduleDoNotDisturbEndJob(initialDelayForJobEnd);
        } else {
            disableDoNotDisturbMode();
            LocalDateTime newEndDateTime = endDateTime.plusSeconds(PERIOD_24_HOURS_AS_SECONDS);
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

    public DoNotDisturbStatus getDisturbStatus() {
        return bellStatus.getDoNotDisturbStatus();
    }

    public void readDoNoDisturbConfigAndReschedule() {
        // Deserialize config async
        bellExecutorService.io.execute(() -> {
            try {
                File confFile = new File(doNotDisturbConfPath);
                if (!confFile.exists()) {
                    log.info("No do not disturb conf file found. Skipping deserialization");
                    return;
                }

                DoNotDisturbStatus doNotDisturbStatus = jacksonObjectMapper.readValue(
                        confFile,
                        DoNotDisturbStatus.class
                );

                log.info("Deserialized DoNotDisturb conf. Rescheduling ...");
                scheduleDoNotDisturb(
                        doNotDisturbStatus.getDays(),
                        doNotDisturbStatus.getStartTimeMillis(),
                        doNotDisturbStatus.getEndTimeMillis(),
                        doNotDisturbStatus.isEndTomorrow()
                );
            } catch (IOException e) {
                log.error("Error while reading do not disturb configuration", e);
            }
        });
    }
    public void enableDoNotDisturbMode() {
        bellStatus.getDoNotDisturbStatus().setInDoNotDisturb(true);
    }

    public void disableDoNotDisturbMode() {
        bellStatus.getDoNotDisturbStatus().setInDoNotDisturb(false);
    }

    public void scheduleDoNotDisturb(int[] days, long startTimeMillis, long endTimeMillis, boolean endTomorrow) {
        try {
            updateDoNotDisturbStatus(days, startTimeMillis, endTimeMillis, endTomorrow);

            Instant startInstant = Instant.ofEpochMilli(startTimeMillis);
            LocalTime startLocalTime = LocalTime.from(startInstant.atZone(ZoneId.systemDefault()));
            LocalDateTime startDateTime = startLocalTime.atDate(LocalDate.now());

            LocalTime endLocalTime = LocalTime.from(Instant.ofEpochMilli(endTimeMillis).atZone(ZoneId.systemDefault()));
            LocalDateTime endDateTime;

            if (endTomorrow) {
                endDateTime = endLocalTime.atDate(LocalDate.now()).plusSeconds(PERIOD_24_HOURS_AS_SECONDS);
            } else {
                endDateTime = endLocalTime.atDate(LocalDate.now());
            }

            if (startDateTime.isEqual(endDateTime) || startDateTime.isAfter(endDateTime)) {
                log.error("Star time must be lower than end time!");
                return;
            }

            startDoNotDisturbJob.getJobParams().putIntArray(KEY_DAYS_ARRAY, bellStatus.getDoNotDisturbStatus().getDays());
            endDoNotDisturbJob.getJobParams().putIntArray(KEY_DAYS_ARRAY, bellStatus.getDoNotDisturbStatus().getDays());
            endDoNotDisturbJob.getJobParams().putLong(
                    KEY_DISTURB_DURATION,
                    Duration.between(startDateTime, endDateTime).getSeconds()
            );

            rescheduleDoNotDisturbStart(startDateTime);
            rescheduleDoNotDisturbEnd(endDateTime);
        } catch (DateTimeException err) {
            log.error("Could not parse start and end time millis for do not disturb!", err);
        }
    }
}
