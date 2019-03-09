package smartbell.restapi.firebase;

import java.util.List;

public interface FirebaseClientManager {
    void registerClientForPushNotifications(String clientGUID, String token);
    List<String> getAllClientTokens();
    void unregisterClientFromPushNotifications(String clientToken);
}
