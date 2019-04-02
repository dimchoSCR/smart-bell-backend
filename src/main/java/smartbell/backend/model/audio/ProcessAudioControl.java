package smartbell.backend.model.audio;

import java.io.IOException;
import java.util.Arrays;
import java.util.Formatter;

public class ProcessAudioControl implements AudioControl {

    private final ProcessBuilder audioAdjustProcessBuilder;
    private final String command;

    private Process process;
    private AudioChannel channel;

    public ProcessAudioControl() {
        this(AudioChannel.PCM);
    }

    public ProcessAudioControl(AudioChannel channel) {
        command = "amixer set %1$s %2$d%%";

        this.channel = channel;
        audioAdjustProcessBuilder = new ProcessBuilder().inheritIO();

    }

    private void updateChangeVolumeCommand(String formattedCommand) {
        audioAdjustProcessBuilder.command().clear();
        audioAdjustProcessBuilder.command().addAll(Arrays.asList(formattedCommand.split("\\s")));
    }

    @Override
    public void setVolumeLevel(int percent) throws IOException {
        if (percent < 0 || percent > 100) {
            throw new IllegalStateException("Percentage out of bounds");
        }

        killAudioAdjustingProcess();

        String formattedCommand = new Formatter().format(command, channel.name(), percent).toString();
        updateChangeVolumeCommand(formattedCommand);
        process = audioAdjustProcessBuilder.start();
    }

    public void killAudioAdjustingProcess() {
        if (process != null) {
            process.destroy();
        }
    }
}
