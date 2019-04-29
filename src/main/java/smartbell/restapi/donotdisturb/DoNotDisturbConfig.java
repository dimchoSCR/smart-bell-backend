package smartbell.restapi.donotdisturb;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import smartbell.restapi.status.DoNotDisturbManager;

import javax.annotation.PostConstruct;

@Configuration
public class DoNotDisturbConfig {

    @Autowired
    private DoNotDisturbManager doNotDisturbManager;

    @PostConstruct
    public void init() {
        doNotDisturbManager.readDoNoDisturbConfigAndReschedule();
    }
}
