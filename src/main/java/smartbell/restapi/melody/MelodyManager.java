package smartbell.spring.melody;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import smartbell.spring.BellServiceException;
import smartbell.spring.storage.StorageService;
import smartbell.spring.SmartBellBackend;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.IOException;
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
        try {
            // Checks if this ringtone is already set
            if(isCurrentRingtone(melodyName)) {
                return "This ringtone has already been set";
            }

            storageService.link(resolvePathToMelody(melodyName), melodyStorageProps.getRingtoneDirPath());

            return "Ringtone set successfully";
        } catch (IOException e) {
            throw new BellServiceException("Could not set file as ringtone!", e);
        }
    }

    public void removeMelody(String melodyName) {

    }

    public MelodyInfo getRingtoneInfo() {
       return null;
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
