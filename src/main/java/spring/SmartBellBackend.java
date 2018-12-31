package spring;

import apps.dimcho.model.GPIO;
import apps.dimcho.model.Pin;
import apps.dimcho.model.PinManager;
import apps.dimcho.model.audio.AudioPlayback;
import apps.dimcho.model.audio.PlaybackMode;
import apps.dimcho.model.audio.ProcessAudioPlayback;
import apps.dimcho.model.kernelinterface.KernelInterfacePinManager;
import org.springframework.stereotype.Component;

@Component
public class SmartBellBackend {
    private final PinManager pinManager;
    private AudioPlayback player;

    public SmartBellBackend() {
        try {
            pinManager = new KernelInterfacePinManager();
        } catch (Exception e) {
            throw new BellServiceException("Error unable to connect to bell kernel interface!", e);
        }
    }

    @SuppressWarnings("Duplicates")
    public void playOnClick(String ringtonePath) {

        if(player != null) {
            throw new BellServiceException("Backend player already playing!");
        }

        player = new ProcessAudioPlayback(ringtonePath, 10);
        try {
            // Export the pin responsible for the bell's button
            Pin pin2 = pinManager.exportPin(GPIO.PIN_2);
            // Enable button debounce
            pin2.setDebounce(100);
            // Listen for button clicks
            pin2.setOnValueChangedListener((value) -> {
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

            throw new BellServiceException("The audio playback failed!" , e);
        }

    }

    public void freeUpResources() {
        if(player != null) {
            player.stop();
        }

        pinManager.resetPins();
    }
}
