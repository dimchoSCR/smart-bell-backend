package smartbell.restapi.donotdisturb;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import smartbell.restapi.job.Job;
import smartbell.restapi.status.DoNotDisturbManager;

@Component
public class StartDoNotDisturbJob extends Job {

    private final Logger log = LoggerFactory.getLogger(StartDoNotDisturbJob.class);

    @Autowired
    private DoNotDisturbManager doNotDisturbManager;

    @Override
    public void doJob() {
        log.info("StartDoNotDisturb running!");
        int[] workingDays = getJobParams().getIntArray(DoNotDisturbManager.KEY_DAYS_ARRAY);
        if (DoNotDisturbUtil.isDoNotDisturbScheduledForToday(workingDays)) {
            log.info("Enabling do not disturb!");
            doNotDisturbManager.enableDoNotDisturbMode();
        }
    }
}
