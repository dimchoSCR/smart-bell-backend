package smartbell.restapi.job;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import sun.rmi.runtime.Log;

import java.util.HashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;

@Service
public class JobManager {

    private final ScheduledExecutorService jobScheduler = Executors.newScheduledThreadPool(2);
    private final HashMap<String, ScheduledFuture> activeJobs = new HashMap<>();

    private final Logger log = LoggerFactory.getLogger(JobManager.class);

    public void schedule(JobRequest jobRequest) {
        Runnable jobRunnable = () -> {
            try {
                jobRequest.getJob().doJob();
            } catch (Exception e) {
                log.error("Job execution failed", e);
                cancelJobById(jobRequest.getRequestId(), true);
            }

            if (!jobRequest.isRecurring()) {
                cancelJobById(jobRequest.getRequestId(), false);
            }
        };

        ScheduledFuture scheduledFuture;
        if (jobRequest.isRecurring()) {
            log.info("Recurring job with id: " + jobRequest.getRequestId() + " scheduled!");
            scheduledFuture = jobScheduler.scheduleAtFixedRate(
                    jobRunnable,
                    jobRequest.getInitialDelay(),
                    jobRequest.getPeriod(),
                    jobRequest.getTimeUnit()
            );
        } else {
            log.info("One time job with id: " + jobRequest.getRequestId() + " scheduled!");
            scheduledFuture = jobScheduler.schedule(
                    jobRunnable,
                    jobRequest.getInitialDelay(),
                    jobRequest.getTimeUnit()
            );

        }

        if (activeJobs.containsKey(jobRequest.getRequestId())) {
            log.error(
                    "Job with id: "
                    + jobRequest.getRequestId() +
                    " is already scheduled! Please cancel job first to reschedule!"
            );

            return;
        }

        activeJobs.put(jobRequest.getRequestId(), scheduledFuture);
    }

    public void cancelJobById(String requestId, boolean cancelNow) {
        log.info("Job with id: " + requestId + " finished!");
        ScheduledFuture scheduledFuture = activeJobs.remove(requestId);
        if (scheduledFuture == null) {
            log.info("Job with id: " + requestId + " is not started or already finished!");
            return;
        }

        scheduledFuture.cancel(cancelNow);
    }


    public void cancelAllJobs(boolean cancelNow) {
        activeJobs.forEach((key, value) -> {
            value.cancel(cancelNow);
        });

        activeJobs.clear();
    }
}
