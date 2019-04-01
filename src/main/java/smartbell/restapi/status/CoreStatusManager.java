package smartbell.restapi.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import smartbell.restapi.BellExecutorService;
import smartbell.restapi.melody.MelodyStorageProperties;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;

@Component
public class CoreStatusManager {

    private static final String CORE_CONFIG_FILE_NAME = "BellConf.json";
    private final Logger log = LoggerFactory.getLogger(CoreStatusManager.class);

    @Autowired
    private BellStatus bellStatus;

    @Autowired
    private ObjectMapper jacksonObjectMapper;

    @Autowired
    private BellExecutorService bellExecutorService;

    @Autowired
    private MelodyStorageProperties melodyStorageProps;

    private String coreConfigStoragePath;

    @PostConstruct
    public void init() {
        coreConfigStoragePath = Paths.get(
                melodyStorageProps.getBaseDirPath(),
                CORE_CONFIG_FILE_NAME
        ).toString();

        bellStatus.getCoreStatus().addPropertyChangeListener(evt -> {
            saveCoreConfigAsync();
        });
    }

    public void saveCoreConfigAsync() {
        // Persist config async
        bellExecutorService.io.execute(() -> {
            try {
                jacksonObjectMapper.writeValue(new File(coreConfigStoragePath), bellStatus.getCoreStatus());
                log.info("Persisted DoNotDisturb conf");
            } catch (IOException e) {
                log.error("Error while saving do not disturb configuration", e);
            }
        });
    }


    public void readCoreConfig() {
        // Deserialize config async
        bellExecutorService.io.execute(() -> {
            try {
                File confFile = new File(coreConfigStoragePath);
                if (!confFile.exists()) {
                    log.info("No bell conf file found. Skipping deserialization");
                    return;
                }

                CoreStatus coreStatus = jacksonObjectMapper.readValue(confFile, CoreStatus.class);
                bellStatus.getCoreStatus().setCurrentRingtone(coreStatus.getCurrentRingtone());
                bellStatus.getCoreStatus().setPlaybackMode(coreStatus.getPlaybackMode());
                bellStatus.getCoreStatus().setRingVolume(coreStatus.getRingVolume());

                log.info("Deserialized Bell conf");

            } catch (IOException e) {
                log.error("Error while reading core configuration", e);
            }
        });
    }
}
