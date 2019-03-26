package smartbell.restapi.firebase;

import org.springframework.stereotype.Component;

@Component
public class BellFirebaseMessages {

    public BellMessage constructRingMessage(boolean isInDoNotDisturb) {
        return new BellMessage(
                NotificationType.RING,
                String.valueOf(isInDoNotDisturb)
        );
    }

    public BellMessage constructDoNotDisturbDataMessage(String data) {
        return new BellMessage(
                NotificationType.MESSAGE,
                data
        );
    }
}
