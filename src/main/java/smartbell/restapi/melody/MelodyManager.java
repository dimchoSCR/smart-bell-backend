package smartbell.restapi.melody;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import smartbell.restapi.BellServiceException;
import smartbell.restapi.storage.StorageService;
import smartbell.restapi.SmartBellBackend;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

@Component
public class MelodyManager {

    @Autowired
    private MelodyStorageProperties melodyStorageProps;
    @Autowired
    private StorageService storageService;
    @Autowired
    private SmartBellBackend smartBellBackend;

    private void validateMusicFile(MultipartFile musicFile) {
        if(musicFile.isEmpty()) {
            throw new BellServiceException("The uploaded file is empty!");
        }

        // MIME type checks
        // TODO refine check Files.probeContentType
        String contentType = musicFile.getContentType();
        if(contentType == null) {
            throw new BellServiceException("No content type provided for file!");
        }

        String contentPrefix = contentType.split("/")[0];
        System.out.println(contentType);
        if(!contentPrefix.equalsIgnoreCase("audio")) {
            throw new BellServiceException("Content type mismatch! Only audio files are permitted.");
        }
    }

    @PostConstruct
    private void init() {
        try {
            // Creates the melody directory for storing multiple ringtones if not created
            storageService.createDirectory(melodyStorageProps.getMelodyStorageDirPath());
            // Creates the "set" ringtone directory if not created
            storageService.createDirectory(melodyStorageProps.getRingtoneDirPath());

            // Listens for raspberryPi button clicks
            playRingtoneOnButtonClick();
        } catch (IOException e) {
            throw new BellServiceException("IO error. Could not create melody directories!", e);
        } catch (Exception e) {
            throw new BellServiceException("Initialization error. " +
                    "Check if the program has permission to read/write files in base directory", e);
        }
    }

    public void addToMelodieLibrary(MultipartFile musicFile) throws BellServiceException {
        try {
            validateMusicFile(musicFile);
            // Overwrites existing melodies
            storageService.store(melodyStorageProps.getMelodyStorageDirPath(), musicFile);
        } catch (IOException e) {
            throw new BellServiceException("IO error. Could not write file!", e);
        }
    }

    public String setAsRingtone(String melodyName) {
        // Checks if this ringtone is already set
        if(isCurrentRingtone(melodyName)) {
            return "This ringtone has already been set";
        }

        MelodyInfo melodyInfo = null;
        boolean uninkWasSuccessful = false;
        try {
            melodyInfo = getRingtoneInfo();
            // Unlink old ringtone if it exists
            if(melodyInfo != null) {
                String oldRingtonePath = resolvePathToRingtone(melodyInfo.getMelodyName());
                storageService.unlink(oldRingtonePath);
                uninkWasSuccessful = true;
            }

            storageService.link(resolvePathToMelody(melodyName), melodyStorageProps.getRingtoneDirPath());

            return "Ringtone set successfully";
        } catch (Exception e) {
            // Reset old ringtone if appropriate
            if(melodyInfo != null && uninkWasSuccessful) {
                try {
                    storageService.link(
                            resolvePathToMelody(melodyInfo.getMelodyName()),
                            melodyStorageProps.getRingtoneDirPath()
                    );
                } catch (IOException innerE) {
                    throw new BellServiceException("Could not reset old ringtone!", e);
                }
            }

            throw new BellServiceException("Could not set file as ringtone!", e);
        }
    }

    public void removeMelody(String melodyName) {

    }

    public MelodyInfo getRingtoneInfo() {
        try {
            Path ringtonePath = storageService.listOnly(melodyStorageProps.getRingtoneDirPath());
            if(ringtonePath == null) {
                return null;
            }

            return new MelodyInfo(ringtonePath.getFileName().toString(), 0, "n/a", true);
        } catch (Exception e) {
            throw new BellServiceException("Could not get ringtone info!", e);
        }
    }

    public List<MelodyInfo> listMelodies() {
        return null;
    }

    public String resolvePathToMelody(String melodyName) {
        return storageService.constructPathStringUsing(
                melodyStorageProps.getMelodyStorageDirPath(),
                melodyName
        );
    }

    public String resolvePathToRingtone(String ringtoneName) {
        return storageService.constructPathStringUsing(
                melodyStorageProps.getRingtoneDirPath(),
                ringtoneName
        );
    }

    public boolean isCurrentRingtone(String melodyName) {
        return storageService.isFile(melodyStorageProps.getRingtoneDirPath(), melodyName);
    }

    public boolean searchMelodyLibraryFor() {
        return true;
    }

    /* --- Backend operations --- */
    public void playRingtoneOnButtonClick() {
        smartBellBackend.playOnClick(melodyStorageProps.getRingtoneDirPath() + "/The_Stratosphere_MP3.mp3");
    }

    @PreDestroy
    public void onPreDestroy() {
        smartBellBackend.freeUpResources();
    }
}
