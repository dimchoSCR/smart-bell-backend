package smartbell.restapi.melody;

import jdk.internal.org.xml.sax.SAXException;
import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.metadata.XMPDM;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import smartbell.backend.model.GPIO;
import smartbell.backend.model.audio.PlaybackMode;
import smartbell.restapi.BackendException;
import smartbell.restapi.BellServiceException;
import smartbell.restapi.storage.StorageService;
import smartbell.restapi.SmartBellBackend;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.File;
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
    private AudioMetadataExtractor audioMetadataExtractor;
    @Autowired
    private SmartBellBackend smartBellBackend;

    @PostConstruct
    private void init() {
        try {

            // Creates the melody directory for storing multiple ringtones if not created
            storageService.createDirectory(melodyStorageProps.getMelodyStorageDirPath());
            // Creates the "set" ringtone directory if not created
            storageService.createDirectory(melodyStorageProps.getRingtoneDirPath());

            initBellBackEnd();
        } catch (IOException e) {
            throw new BellServiceException("IO error. Could not create melody directories!", e);
        } catch (BackendException e) {
            throw new BellServiceException("Initialization error. Could not initialize backend! " +
                    "Check if the program has permission to read/write files in base directory", e);
        } catch (Exception e) {
            throw new BellServiceException("Unknown error during backend initialization!", e);
        }
    }

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

    private String resolvePathToMelody(String melodyName) throws IOException {
        return storageService.constructPathStringUsing(
                melodyStorageProps.getMelodyStorageDirPath(),
                melodyName
        );
    }

    private String resolvePathToRingtone(String ringtoneName) throws IOException {
        return storageService.constructPathStringUsing(
                melodyStorageProps.getRingtoneDirPath(),
                ringtoneName
        );
    }

    private String getPathToRingtone()  {
        try {
            return storageService.getPathToOnlyFileInDir(melodyStorageProps.getRingtoneDirPath());
        } catch (Exception e) {
            throw new BellServiceException("Could not get path to ringtone", e);
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

        String oldRingtoneFilePath = getPathToRingtone();
        boolean unlinkWasSuccessful = false;
        try {
            // Unlink old ringtone if it exists
            if(oldRingtoneFilePath != null) {
                storageService.unlink(oldRingtoneFilePath);
                unlinkWasSuccessful = true;
            }

            storageService.link(resolvePathToMelody(melodyName), melodyStorageProps.getRingtoneDirPath());
            // Set backend player to play the new ringtone
            smartBellBackend.updatePlayerRingtone(resolvePathToRingtone(melodyName));

            return "Ringtone set successfully";
        } catch (IOException e) {
            // Rollback old ringtone if appropriate
            if(unlinkWasSuccessful) {
                try {
                    String oldMelodyName = oldRingtoneFilePath.substring(
                            oldRingtoneFilePath.lastIndexOf(File.separatorChar) + 1
                    );

                    storageService.link(resolvePathToMelody(oldMelodyName), melodyStorageProps.getRingtoneDirPath());
                } catch (IOException innerE) {
                    throw new BellServiceException("Setting previous ringtone failed!", innerE);
                }

                return "Error could not set provided ringtone. Reverted to old one";
            }

            return "Error could not set provided ringtone. Did you upload any?";

        } catch (BackendException e) {
            throw new BellServiceException("Could not set file as ringtone! ", e);
        } catch (Exception e) {
            throw new BellServiceException("Unknown error while setting ringtone", e);
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

            String ringtoneName = ringtonePath.getFileName().toString();
            String ringtoneFilePath = ringtonePath.toAbsolutePath().toString();

            Metadata ringtoneMetadata = audioMetadataExtractor.parseAudioFileMetadata(ringtoneFilePath);
            String ringtoneDuration = ringtoneMetadata.get(XMPDM.DURATION);

            long ringtoneFileSize = storageService.getFileSize(ringtoneFilePath);

            return new MelodyInfo(ringtoneName, ringtoneFileSize, ringtoneDuration, true);
        } catch (TikaException | SAXException | IOException e){
          throw new BellServiceException("Could not extract ringtone metadata!", e);
        } catch (Exception e) {
            throw new BellServiceException("Could not get ringtone info!", e);
        }
    }

    public List<MelodyInfo> listMelodies() {
        return null;
    }

    public boolean isCurrentRingtone(String melodyName) {
        return storageService.isFile(melodyStorageProps.getRingtoneDirPath(), melodyName);
    }

    public boolean searchMelodyLibraryFor() {
        return true;
    }

    /* --- Backend operations --- */
    private void initBellBackEnd() throws BackendException, BellServiceException {
        String pathToNewRingtone = getPathToRingtone();
        if(pathToNewRingtone == null) {
            pathToNewRingtone = ""; // TODO maybe a default ringtone
        }

        // Set current ringtone to be played
        smartBellBackend.updatePlayerRingtone(pathToNewRingtone);
        smartBellBackend.setPlayerMode(PlaybackMode.MODE_STOP_AFTER_DELAY);
        // Listens for raspberryPi button clicks
        smartBellBackend.initializeBellButtonListener(GPIO.PIN_2, 100);
    }

    @PreDestroy
    public void onPreDestroy() {
        smartBellBackend.freeUpResources();
    }
}
