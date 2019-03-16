package smartbell.restapi.donotdisturb;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import smartbell.restapi.job.Job;

@Component
public class EndDoNotDisturbJob extends Job {

    private final Logger log = LoggerFactory.getLogger(EndDoNotDisturbJob.class);

    @Autowired
    private DoNotDisturbManager doNotDisturbManager;

    @Override
    public void doJob() {
        log.info("Disabling do not disturb!");
        doNotDisturbManager.disableDoNotDisturbMode();
    }
}
