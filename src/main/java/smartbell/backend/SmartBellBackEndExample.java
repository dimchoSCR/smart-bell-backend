package smartbell.backend;

import smartbell.backend.model.GPIO;
import smartbell.backend.model.audio.AudioPlayback;
import smartbell.backend.model.audio.PlaybackMode;
import smartbell.backend.model.audio.ProcessAudioPlayback;
import smartbell.backend.model.kernelinterface.KernelInterfacePinManager;
import smartbell.backend.model.Pin;
import smartbell.backend.model.PinManager;

public class SmartBellBackEndExample {
    @SuppressWarnings("Duplicates")
    public static void main(String[] args) {
        PinManager pinManager = null;
        AudioPlayback player = new ProcessAudioPlayback("mpg123",
                "/home/pi/The_Stratosphere_MP3.mp3");
        try {
            pinManager = new KernelInterfacePinManager();
            Pin pin2 = pinManager.exportPin(GPIO.PIN_2);
            pin2.setDebounce(100);
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

            Thread.sleep(30000);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            player.stop();

            if(null != pinManager) {
                pinManager.resetPins();
            }
        }
    }
}
