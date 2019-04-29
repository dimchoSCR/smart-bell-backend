package smartbell.restapi.donotdisturb;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import smartbell.restapi.firebase.FirebaseNotificationService;
import smartbell.restapi.job.Job;
import smartbell.restapi.status.DoNotDisturbManager;

import java.time.Clock;
import java.time.LocalDateTime;

@Component
public class EndDoNotDisturbJob extends Job {

    private final Logger log = LoggerFactory.getLogger(EndDoNotDisturbJob.class);

    @Autowired
    private DoNotDisturbManager doNotDisturbManager;

    @Autowired
    private FirebaseNotificationService firebaseNotificationService;

    @Override
    public void doJob() {
        log.info("EndDoNotDisturb running!");

        int[] workingDays = getJobParams().getIntArray(DoNotDisturbManager.KEY_DAYS_ARRAY);
        if (DoNotDisturbUtil.isDoNotDisturbScheduledForToday(workingDays)) {
            log.info("Disabling do not disturb!");
            doNotDisturbManager.disableDoNotDisturbMode();

            log.info("Sending missed rings if any!");

            long disturbDuration = getJobParams().getLong(DoNotDisturbManager.KEY_DISTURB_DURATION, 0);
            firebaseNotificationService.sendDoNotDisturbReportFrom(LocalDateTime.now(Clock.systemUTC()).minusSeconds(disturbDuration));
        }
    }
}
