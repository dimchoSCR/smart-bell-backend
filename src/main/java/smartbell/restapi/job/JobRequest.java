package smartbell.restapi.job;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class JobRequest {

    private boolean recurring;
    private long initialDelay;
    private long period;

    private String requestId;
    private TimeUnit timeUnit;
    private final Job job;

    private JobRequest(Job job) {
        recurring = false;
        initialDelay = 0;
        period = 0;
        requestId = UUID.randomUUID().toString();
        timeUnit = TimeUnit.MILLISECONDS;
        this.job = job;
    }

    public boolean isRecurring() {
        return recurring;
    }

    public void setRecurring(boolean recurring) {
        this.recurring = recurring;
    }

    public long getInitialDelay() {
        return initialDelay;
    }

    public void setInitialDelay(long initialDelay) {
        this.initialDelay = initialDelay;
    }

    public long getPeriod() {
        return period;
    }

    public void setPeriod(long period) {
        this.period = period;
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public TimeUnit getTimeUnit() {
        return timeUnit;
    }

    public void setTimeUnit(TimeUnit timeUnit) {
        this.timeUnit = timeUnit;
    }

    public Job getJob() {
        return job;
    }

    public static class Builder {
        private final JobRequest builtJobRequest;

        public Builder(Job job) {
            builtJobRequest = new JobRequest(job);
        }

        public JobRequest build() {
            JobRequest jobRequest = new JobRequest(builtJobRequest.job);

            jobRequest.setRequestId(builtJobRequest.requestId);
            jobRequest.setRecurring(builtJobRequest.recurring);
            jobRequest.setInitialDelay(builtJobRequest.initialDelay);
            jobRequest.setPeriod(builtJobRequest.period);
            jobRequest.setTimeUnit(builtJobRequest.timeUnit);

            return jobRequest;
        }

        public JobRequest.Builder setRequestId(String requestId) {
            builtJobRequest.setRequestId(requestId);
            return this;
        }

        public JobRequest.Builder setStartDelay(long delay) {
            builtJobRequest.setInitialDelay(delay);
            return this;
        }

        public JobRequest.Builder setRecurringPeriod(long period) {
            builtJobRequest.setPeriod(period);
            builtJobRequest.setRecurring(true);
            return this;
        }

        public JobRequest.Builder setTimeUnit(TimeUnit timeUnit) {
            builtJobRequest.setTimeUnit(timeUnit);
            return this;
        }
    }
}
