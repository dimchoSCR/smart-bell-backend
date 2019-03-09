package smartbell.restapi.firebase;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import smartbell.restapi.BellServiceException;
import smartbell.restapi.db.SmartBellRepository;

import java.util.List;

@Component
public class FirebaseAppInstanceManager implements FirebaseClientManager {

    @Autowired
    private SmartBellRepository smartBellRepository;

    @Override
    public void registerClientForPushNotifications(String clientGUID, String token) {
        int affectedRows = smartBellRepository.registerAppInstance(clientGUID, token);

        if (affectedRows <= 0 || affectedRows > 1) {
            throw new BellServiceException("Bad affected rows count: " + affectedRows);
        }
    }

    @Override
    public List<String> getAllClientTokens() {
        return smartBellRepository.getAllAppInstanceTokens();
    }

    @Override
    public void unregisterClientFromPushNotifications(String clientToken) {
        int affectedRows = smartBellRepository.removeAppInstanceByToken(clientToken);

        if (affectedRows == 0) {
            throw new BellServiceException("Removing app instance using token failed! Does the token exist?");
        }

        if (affectedRows > 1) {
            throw new BellServiceException("Multiple app instances were removed using a single token!");
        }
    }
}
