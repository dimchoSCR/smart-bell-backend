package smartbell.restapi.donotdisturb;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import smartbell.restapi.job.Job;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

@Component
public class StartDoNotDisturbJob extends Job {

    private static final Map<String, Integer> weekDayNameToNumber = new HashMap<>();
    static {
        weekDayNameToNumber.put("Mon", 0);
        weekDayNameToNumber.put("Tue", 1);
        weekDayNameToNumber.put("Wed", 2);
        weekDayNameToNumber.put("Thu", 3);
        weekDayNameToNumber.put("Fri", 4);
        weekDayNameToNumber.put("Sat", 5);
        weekDayNameToNumber.put("Sun", 6);
    }

    private final Logger log = LoggerFactory.getLogger(StartDoNotDisturbJob.class);

    @Autowired
    private DoNotDisturbManager doNotDisturbManager;

    private boolean shouldEnableDoNotDisturbToday() {
        int[] days = getJobParams().getIntArray(DoNotDisturbManager.KEY_DAYS_ARRAY);
        String weekDayName = new SimpleDateFormat("EEE").format(Calendar.getInstance().getTime());
        for (int day : days) {
            if (day == weekDayNameToNumber.get(weekDayName)) {
                return true;
            }
        }

        return false;
    }

    @Override
    public void doJob() {
        log.info("StartDoNotDisturb running!");
        if (shouldEnableDoNotDisturbToday()) {
            log.info("Enabling do not disturb!");
            doNotDisturbManager.enableDoNotDisturbMode();
        }
    }
}
