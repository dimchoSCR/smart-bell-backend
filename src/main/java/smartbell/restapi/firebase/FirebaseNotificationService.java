package smartbell.restapi.firebase;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import smartbell.restapi.SmartBellBackend;

import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

@Service
public class FirebaseNotificationService {

    private static final String FIREBASE_NOTIFICATION_TYPE = "NotificationType";

    private final Logger logger = LoggerFactory.getLogger(SmartBellBackend.class);
    private final Executor notifyExecutor = Executors.newSingleThreadExecutor();

    @Autowired
    private FirebaseClientManager firebaseClientManager;

    private enum NotificationType {
        RING, MESSAGE
    }

    private enum FirebaseErrorCode {

        INVALID_TOKEN("invalid-registration-token"),
        TOKEN_NOT_REGISTERED("registration-token-not-registered"),
        UNCLASSIFIED("unclassified");

        private final String value;
        FirebaseErrorCode(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }

        public static FirebaseErrorCode fromString(String error) {
            for (FirebaseErrorCode errorCode : values()) {
                if (errorCode.getValue().equalsIgnoreCase(error)) {
                    return errorCode;
                }
            }

            return FirebaseErrorCode.UNCLASSIFIED;
        }
    }

    public void sendPushNotificationAsync() {
        // TODO do not disturb flag
        notifyExecutor.execute(() -> {
            try {
                List<String> appInstanceTokens = firebaseClientManager.getAllClientTokens();
                for (String token : appInstanceTokens) {
                    Message message = Message.builder()
                            .putData(FIREBASE_NOTIFICATION_TYPE, NotificationType.RING.name())
                            .setToken(token)
                            .build();

                    // Handle exception here so resuming the loop is possible
                    // Resumption is needed when FCM renews a token
                    try {
                        logger.info("Sending notification to: " + token);
                        // Send a message to the devices identified by each token
                        FirebaseMessaging.getInstance().send(message);
                    } catch (FirebaseMessagingException e) {
                        FirebaseErrorCode errorCode = FirebaseErrorCode.fromString(e.getErrorCode());

                        // Unregister invalid app instance pairs (appGUID => token)
                        if (errorCode == FirebaseErrorCode.INVALID_TOKEN || errorCode == FirebaseErrorCode.TOKEN_NOT_REGISTERED) {
                            if (token != null) {
                                firebaseClientManager.unregisterClientFromPushNotifications(token);
                                logger.info("Inactive token removed from db: " + token);
                            }
                        } else {
                            logger.error("Unhandled firebase error occurred!", e);
                            break;
                        }
                    }
                }
            } catch (Exception e) {
                logger.error("An error occurred while sending push notification", e);
            }
        });
    }
}
