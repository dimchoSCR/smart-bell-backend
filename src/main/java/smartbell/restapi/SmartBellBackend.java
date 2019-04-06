package smartbell.restapi;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import smartbell.backend.model.GPIO;
import smartbell.backend.model.Pin;
import smartbell.backend.model.PinManager;
import smartbell.backend.model.audio.AudioControl;
import smartbell.backend.model.audio.PlaybackMode;
import smartbell.backend.model.audio.ProcessAudioControl;
import smartbell.backend.model.audio.ProcessAudioPlayback;
import smartbell.backend.model.kernelinterface.KernelInterfacePinManager;
import org.springframework.stereotype.Component;
import smartbell.restapi.exceptions.BackendException;
import smartbell.restapi.firebase.BellFirebaseMessages;
import smartbell.restapi.firebase.FirebaseNotificationService;
import smartbell.restapi.log.RingLogManager;
import smartbell.restapi.status.BellStatus;

import java.io.IOException;

@Component
public class SmartBellBackend {
    private final Logger log = LoggerFactory.getLogger(SmartBellBackend.class);

    private final PinManager pinManager;
    private final ProcessAudioPlayback player;
    private final ProcessAudioPlayback prePlayPlayer;
    private final ProcessAudioControl audioControl;

    private Pin bellButtonPin;
    private Pin audioBlockPin;

    @Autowired
    private RingLogManager ringLogManager;

    @Autowired
    private FirebaseNotificationService notificationService;

    @Autowired
    private BellStatus bellStatus;

    @Autowired
    private BellFirebaseMessages bellFirebaseMessages;


    public SmartBellBackend() throws BackendException {
        try {
            pinManager = new KernelInterfacePinManager();
            // Stop audio output using hardware to prevent noise
            audioBlockPin = pinManager.provisionPin(GPIO.PIN_27, Pin.Direction.OUT);
        } catch (Exception e) {
            throw new BackendException("Error unable to connect to bell kernel interface!", e);
        }

        player = new ProcessAudioPlayback();

        prePlayPlayer = new ProcessAudioPlayback();
        prePlayPlayer.setPlaybackMode(PlaybackMode.MODE_STOP_AFTER_DELAY);
        prePlayPlayer.setPlaybackStopTime(30);
        prePlayPlayer.setOnStopListener(this::enableSoundBlock);

        audioControl = new ProcessAudioControl();
    }

    private void enableSoundBlock() {
        try {
            audioBlockPin.setValue(Pin.Value.LOW);
        } catch (Exception e) {
            log.error("Could not set pin to low!", e);
        }
    }

    @SuppressWarnings("Duplicates")
    public void initializeBellButtonListener(GPIO buttonPinNumber, int debounce) throws BackendException {
        try {
            // Export the pin responsible for the bell's button
            bellButtonPin = pinManager.exportPin(buttonPinNumber);
            // Enable button debounce
            bellButtonPin.setDebounce(debounce);

            player.setOnStopListener(this::enableSoundBlock);

            // Listen for button clicks
            bellButtonPin.setOnValueChangedListener((value) -> {
                try {
                    String playingMelodyName = player.getCurrentSongName();
                    if (value == 1 && !playingMelodyName.isEmpty() && !player.isPlaying()) {
                        boolean isInDoNotDisturb = bellStatus.getDoNotDisturbStatus().isInDoNotDisturb();
                        if (!isInDoNotDisturb) {
                            prePlayPlayer.stop();
                            audioBlockPin.setValue(Pin.Value.HIGH);
                            player.play();
                        }
                        ringLogManager.addToRingLogAsync(playingMelodyName);
                        notificationService.sendPushNotificationAsync(bellFirebaseMessages.constructRingMessage(isInDoNotDisturb));
                    } else if (player.getPlaybackMode() == PlaybackMode.MODE_STOP_ON_RELEASE) {
                        player.stop();
                    }
                } catch (Exception e) {
                    log.error("Error occurred while listening for button clicks", e);
                    player.stop();
                }
            });

        } catch (Exception e) {
            freeUpResources();
            throw new BackendException("The audio playback failed!" , e);
        }
    }

    public void updatePlayerRingtone(String pathToNewRingtone) throws BackendException {
        try {
            player.updateAudio(pathToNewRingtone);
        } catch (Exception e) {
            freeUpResources();
            throw new BackendException("Changing audio file failed!", e);
        }
    }

    public void setPlayerMode(PlaybackMode playbackMode) {
        player.setPlaybackMode(playbackMode);
    }
    public void setPlayerPlaybackTime(int seconds) {
        player.setPlaybackStopTime(seconds);
    }

    public void setRingVolume(int percent) throws BackendException {
        try {
            audioControl.setVolumeLevel(percent);
        } catch (Exception e) {
            audioControl.killAudioAdjustingProcess();
            throw new BackendException("Changing hardware volume failed!", e);
        }
    }

    public void prePlay(String pathMelody) throws BackendException {
        try {
            if (!prePlayPlayer.isPlaying() && !player.isPlaying()) {
                prePlayPlayer.updateAudio(pathMelody);
                audioBlockPin.setValue(Pin.Value.HIGH);
                prePlayPlayer.play();
            }
        } catch (Exception e) {
            freeUpResources();
            throw new BackendException("Could start playing melody!", e);
        }
    }

    public void stopPrePlay() {
        if (!player.isPlaying()) {
            prePlayPlayer.stop();
        }
    }

    public void freeUpResources() {
        player.stop();
        player.removeOnStopListener();
        pinManager.resetPins();
    }
}
