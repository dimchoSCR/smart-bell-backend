package smartbell.restapi.job;

public abstract class Job {
    private final JobParams jobParams = new JobParams();

    public abstract void doJob();

    public JobParams getJobParams() {
        return jobParams;
    }
}
