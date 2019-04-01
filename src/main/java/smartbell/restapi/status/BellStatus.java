package smartbell.restapi.status;

import org.springframework.stereotype.Component;

@Component
public class BellStatus {

    private final CoreStatus coreStatus;
    private final DoNotDisturbStatus doNotDisturbStatus;

    BellStatus() {
        this.coreStatus = new CoreStatus();
        this.doNotDisturbStatus = new DoNotDisturbStatus();
    }

    public CoreStatus getCoreStatus() {
        return coreStatus;
    }

    public DoNotDisturbStatus getDoNotDisturbStatus() {
        return doNotDisturbStatus;
    }
}
