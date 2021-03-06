package smartbell.restapi.firebase;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import smartbell.restapi.SmartBellBackend;
import smartbell.restapi.db.ComparisonSigns;
import smartbell.restapi.db.SmartBellRepository;
import smartbell.restapi.db.entities.RingEntry;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

@Service
public class FirebaseNotificationService {

    private static final String FIREBASE_NOTIFICATION_TYPE = "NotificationType";
    private static final String KEY_NOTIFICATION_DATA = "Data";

    private final Logger logger = LoggerFactory.getLogger(SmartBellBackend.class);
    private final Executor notifyExecutor = Executors.newSingleThreadExecutor();

    @Autowired
    private FirebaseClientManager firebaseClientManager;

    @Autowired
    private SmartBellRepository smartBellRepository;

    @Autowired
    private ObjectMapper jacksonObjectMapper;

    @Autowired
    private BellFirebaseMessages bellFirebaseMessages;

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

    public void sendPushNotificationAsync(BellMessage bellMessage) {
        notifyExecutor.execute(() -> {
            try {
                List<String> appInstanceTokens = firebaseClientManager.getAllClientTokens();
                Message.Builder messageBuilder = Message.builder()
                        .putData(FIREBASE_NOTIFICATION_TYPE, bellMessage.getNotificationType().name())
                        .putData(KEY_NOTIFICATION_DATA, bellMessage.getData());

                for (String token : appInstanceTokens) {
                    Message message = messageBuilder.setToken(token).build();

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

    public void sendDoNotDisturbReportFrom(LocalDateTime queryTimeUTC) {
        String dateTimeString = queryTimeUTC.format(DateTimeFormatter.ISO_DATE_TIME);

        List<RingEntry> ringEntries = smartBellRepository.getRingEntriesBasedOn(ComparisonSigns.GRATER_THAN_OR_EQUALS, dateTimeString);
        if (ringEntries.isEmpty()) {
            logger.info("No missed rings!");
            return;
        }

        try {
            String ringEntriesJSON = jacksonObjectMapper.writeValueAsString(ringEntries);
            BellMessage bellMessage = bellFirebaseMessages.constructDoNotDisturbDataMessage(ringEntriesJSON);
            sendPushNotificationAsync(bellMessage);
        } catch (JsonProcessingException e) {
            logger.error("Could not serialize Ring entries!", e);
        }
    }

}
