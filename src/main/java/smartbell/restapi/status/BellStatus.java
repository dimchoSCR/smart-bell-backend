package smartbell.restapi.status;

import org.springframework.stereotype.Component;

@Component
public class BellStatus {

    private final DoNotDisturbStatus doNotDisturbStatus;

    BellStatus() {
        this.doNotDisturbStatus = new DoNotDisturbStatus();
    }

    public DoNotDisturbStatus getDoNotDisturbStatus() {
        return doNotDisturbStatus;
    }
}
