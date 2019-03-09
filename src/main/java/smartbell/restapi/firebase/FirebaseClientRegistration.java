package smartbell.restapi.firebase;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/notifications")
public class FirebaseClientRegistration {

    @Autowired
    private FirebaseClientManager firebaseClientManager;

    @PutMapping("/register")
    public void registerAppForPushNotifications(@RequestParam String appGUID, @RequestParam String firebaseToken) {
        firebaseClientManager.registerClientForPushNotifications(appGUID, firebaseToken);
    }
}
