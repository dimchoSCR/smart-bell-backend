package smartbell.restapi.melody;

import jdk.internal.org.xml.sax.SAXException;
import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.mime.MediaType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import smartbell.backend.model.GPIO;
import smartbell.backend.model.audio.PlaybackMode;
import smartbell.restapi.exceptions.BackendException;
import smartbell.restapi.exceptions.BellServiceException;
import smartbell.restapi.SmartBellBackend;
import smartbell.restapi.status.BellStatus;
import smartbell.restapi.status.CoreStatusManager;
import smartbell.restapi.storage.StorageService;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
    @Autowired
    private BellStatus bellStatus;
    @Autowired
    private CoreStatusManager coreStatusManager;

    private final Logger log = LoggerFactory.getLogger(MelodyManager.class);

    @PostConstruct
    private void init() {
        try {

            // Creates the melody directory for storing multiple ringtones if not created
            storageService.createDirectory(melodyStorageProps.getMelodyStorageDirPath());
            // Creates the "set" ringtone directory if not created
            storageService.createDirectory(melodyStorageProps.getRingtoneDirPath());

            String pathToNewRingtone = getPathToRingtone();
            if(pathToNewRingtone == null) {
                pathToNewRingtone = ""; // TODO maybe a default ringtone
            } else {
                bellStatus.getCoreStatus().setCurrentRingtone(getRingtoneInfo().getMelodyName());
            }

            initBellBackEnd(pathToNewRingtone);
            coreStatusManager.initializeCoreConfChangeListener();
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
        try {
            // Wrap InputStream in BufferedInputStream to enable mark and reset operations
            MediaType mediaType = audioMetadataExtractor.detectContentType(
                    new BufferedInputStream(musicFile.getInputStream())
            );

            String mimeType = mediaType.getType();
            if(mimeType.isEmpty()) {
                throw new BellServiceException("Could not determine file content type!");
            }

            if(!mimeType.equalsIgnoreCase(AudioMetadataExtractor.AUDIO_BASE_CONTENT_TYPE)) {
                throw new BellServiceException("Content type mismatch! Only audio files are permitted.");
            }
        } catch (IOException e) {
            throw new BellServiceException("Could not determine file content type", e);
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

    private MelodyInfo constructMelodyInfoFor(Path melodyPath)  {
        // TODO duration
        try {
            String melodyName = melodyPath.getFileName().toString();
            String melodyFilePath = melodyPath.toAbsolutePath().toString();

            long melodyFileSize = storageService.getFileSize(melodyFilePath);
            boolean ringtone = isCurrentRingtone(melodyName);
            Metadata melodyMetadata = audioMetadataExtractor.parseAudioFileMetadata(melodyFilePath);
            String melodyDuration = audioMetadataExtractor.tryExtractingDuration(melodyMetadata, melodyFilePath);
            String melodyType = melodyMetadata.get(AudioMetadataExtractor.META_CONTENT_TYPE);

            return new MelodyInfo(melodyName, melodyType, melodyFileSize, melodyDuration, ringtone);
        } catch (TikaException | SAXException | IOException e){
            throw new BellServiceException("Could not get melody info! Extracting metadata failed", e);
        } catch (Exception e) {
            throw new BellServiceException("Could not get melody info!", e);
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
            // Update Bell status
            bellStatus.getCoreStatus().setCurrentRingtone(melodyName);

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
        Path ringtonePath;
        try {
            ringtonePath = storageService.listOnly(melodyStorageProps.getRingtoneDirPath());
            if(ringtonePath == null) {
                return null;
            }
        } catch (Exception e) {
            throw new BellServiceException("Could not get ringtone info!", e);
        }

        return constructMelodyInfoFor(ringtonePath);
    }

    public List<MelodyInfo> listMelodies() {
        Stream<Path> audioFiles;
        try {
            audioFiles = storageService.listAll(melodyStorageProps.getMelodyStorageDirPath());
        } catch (IOException e) {
            throw new BellServiceException("Could not list melodies!", e);
        }

        // Returns an empty list if no melodies exist
        return audioFiles.map(this::constructMelodyInfoFor)
                .collect(Collectors.toList());
    }

    public boolean isCurrentRingtone(String melodyName) {
        return storageService.isFile(melodyStorageProps.getRingtoneDirPath(), melodyName);
    }

    public void deleteMelody(String melodyName) {
        try {
            MelodyInfo ringtoneInfo = getRingtoneInfo();
            if (ringtoneInfo != null) {
                boolean isRingtone = ringtoneInfo.getMelodyName().equalsIgnoreCase(melodyName);
                if (isRingtone) {
                    String linkToRingTone = resolvePathToRingtone(melodyName);
                    storageService.unlink(linkToRingTone);
                }
            }

            String actualPath = resolvePathToMelody(melodyName);
            storageService.delete(actualPath);
        } catch (IOException e) {
           throw new BellServiceException("Could not delete melody", e);
        }
    }

    public boolean searchMelodyLibraryFor() {
        return true;
    }

    public BellStatus getBellStatus() {
        return bellStatus;
    }

    /* --- Backend operations --- */
    private void initBellBackEnd(String pathToRingtone) throws BackendException, BellServiceException {
        // Set current ringtone to be played
        smartBellBackend.updatePlayerRingtone(pathToRingtone);
        smartBellBackend.setPlayerMode(PlaybackMode.valueOf(bellStatus.getCoreStatus().getPlaybackMode()));
        smartBellBackend.setPlayerPlaybackTime(bellStatus.getCoreStatus().getPlaybackTime());
        smartBellBackend.setRingVolume(bellStatus.getCoreStatus().getRingVolume());
        // Listens for raspberryPi button clicks
        // TODO apply debounce from server preference
        smartBellBackend.initializeBellButtonListener(GPIO.PIN_0, 100);
    }

    public void setBellPlaybackMode(String playbackMode) {
        bellStatus.getCoreStatus().setPlaybackMode(playbackMode);
        smartBellBackend.setPlayerMode(PlaybackMode.valueOf(playbackMode));
    }

    public void setBellPlaybackDuration(int duration) {
        if (duration < 10 || duration > 60) {
            throw new BellServiceException("Playback duration must be betweeen 10 and 60!");
        }

        bellStatus.getCoreStatus().setPlaybackTime(duration);
        smartBellBackend.setPlayerPlaybackTime(duration);
    }

    public void setBellVolume(int percent) {
        try {
            if (percent < 0 || percent > 100) {
                throw new BellServiceException("Volume must be in the range [0, 100]");
            }

            bellStatus.getCoreStatus().setRingVolume(percent);
            smartBellBackend.setRingVolume(percent);
        } catch (BackendException e) {
            throw new BellServiceException("Setting bell volume failed", e);
        }
    }

    public void startMelodyPrePlay(String melodyName) {
        try {
            smartBellBackend.prePlay(resolvePathToMelody(melodyName));
        } catch (BackendException | IOException e) {
            throw new BellServiceException("Could not pre-play melody", e);
        }
    }

    public void endMelodyPrePlay() {
        smartBellBackend.stopPrePlay();
    }

    @PreDestroy
    public void onPreDestroy() {
        smartBellBackend.freeUpResources();
    }
}
