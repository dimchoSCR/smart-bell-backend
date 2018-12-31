package spring.melody;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import java.nio.file.Paths;


@Component
@Configuration
@ConfigurationProperties("melodies")
public class MelodyStorageProperties {
    private static final String USER_HOME_DIR_PATH = System.getProperty("user.home");
    private static final String DEFAULT_BASE_DIR_NAME = "SmartBell";
    private static final String DEFAULT_MELODY_STORAGE_DIR_NAME = "AllMelodies";
    private static final String RINGTONE_DIR_NAME = "Ringtone";

    private String baseDirPath;
    private String melodyStorageDirPath;

    MelodyStorageProperties() {
        // Initialize default value for the properties
        // If values are specified in the properties file the default values are overridden
        baseDirPath = Paths.get(USER_HOME_DIR_PATH, DEFAULT_BASE_DIR_NAME).toString();
        melodyStorageDirPath = Paths.get(baseDirPath, DEFAULT_MELODY_STORAGE_DIR_NAME).toString();
    }

    public String getBaseDirPath() {
        return baseDirPath;
    }

    public void setBaseDirPath(String baseDirPath) {
        this.baseDirPath = baseDirPath;
    }

    public String getMelodyStorageDirPath() {
        return melodyStorageDirPath;
    }

    public void setMelodyStorageDirPath(String melodyStorageDirPath) {
        this.melodyStorageDirPath = melodyStorageDirPath;
    }

    public String getRingtoneDirPath() {
        return Paths.get(baseDirPath, RINGTONE_DIR_NAME).toString();
    }
}
