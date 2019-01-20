package smartbell.restapi;

import smartbell.backend.model.GPIO;
import smartbell.backend.model.Pin;
import smartbell.backend.model.PinManager;
import smartbell.backend.model.audio.PlaybackMode;
import smartbell.backend.model.audio.ProcessAudioPlayback;
import smartbell.backend.model.kernelinterface.KernelInterfacePinManager;
import org.springframework.stereotype.Component;

@Component
public class SmartBellBackend {
    private final PinManager pinManager;
    private final ProcessAudioPlayback player;

    private Pin bellButtonPin;

    public SmartBellBackend() throws BackendException {
        try {
            pinManager = new KernelInterfacePinManager();
        } catch (Exception e) {
            throw new BackendException("Error unable to connect to bell kernel interface!", e);
        }

        player = new ProcessAudioPlayback();
    }

    @SuppressWarnings("Duplicates")
    public void initializeBellButtonListener(GPIO buttonPinNumber, int debounce) throws BackendException {
        try {
            // Export the pin responsible for the bell's button
            bellButtonPin = pinManager.exportPin(buttonPinNumber);
            // Enable button debounce
            bellButtonPin.setDebounce(debounce);

            // Listen for button clicks
            bellButtonPin.setOnValueChangedListener((value) -> {
                try {
                    if (value == 1) {
                        player.play();
                    } else if(player.getPlaybackMode() == PlaybackMode.MODE_STOP_ON_RELEASE) {
                        player.stop();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    player.stop();
                }
            });

        } catch (Exception e) {
            player.stop();
            pinManager.resetPins();

            throw new BackendException("The audio playback failed!" , e);
        }
    }

    public void updatePlayerRingtone(String pathToNewRingtone) throws BackendException {
        try {
            player.updateAudio(pathToNewRingtone);
        } catch (Exception e) {
            player.stop();
            pinManager.resetPins();

            throw new BackendException("Changing audio file failed!", e);
        }
    }

    public void setPlayerMode(PlaybackMode playbackMode) {
        player.setPlaybackMode(playbackMode);
    }

    public void freeUpResources() {
        player.stop();
        pinManager.resetPins();
    }
}
