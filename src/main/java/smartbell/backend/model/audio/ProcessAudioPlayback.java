package smartbell.backend.model.audio;

import java.util.*;

public class ProcessAudioPlayback implements AudioPlayback {
    /* mpg123 options
    "--loop", "-1"
     */

    private static final int DEFAULT_PLAYBACK_TIME_SEC = 10;

    public static final PlaybackMode DEFAULT_PLAYBACK_MODE = PlaybackMode.MODE_STOP_ON_RELEASE;
    public static final String DEFAULT_AUDIO_PLAYER = "cvlc";
    public static final String[] DEFAULT_PLAYER_OPTIONS = new String[] {
            "-I", "dummy",
            "-A",  "alsa",
            "--loop", "--play-and-exit",
            "--no-auto-preparse",
            "--no-interact"
    };

    private final ProcessBuilder playbackProcessBuilder;

    private PlaybackMode mode;
    private Timer timer;
    private Process process;
    private final String[] options;

    private String audioFilePath;
    private int playbackTimeSec;
    private boolean optionsBeforeFilePath;

    public ProcessAudioPlayback() {
        this("");
    }

    public ProcessAudioPlayback(String audioFilePath) {
        this(DEFAULT_AUDIO_PLAYER, audioFilePath);
    }

    public ProcessAudioPlayback(String audioFilePath, int playbackTimeSec) {
        this(DEFAULT_AUDIO_PLAYER, audioFilePath, playbackTimeSec);
    }

    public ProcessAudioPlayback(String audioPlayer, String audioFilePath) {
        this(audioPlayer, audioFilePath, DEFAULT_PLAYBACK_MODE);
    }

    public ProcessAudioPlayback(String audioPlayer, String audioFilePath, PlaybackMode mode) {
        this(audioPlayer, audioFilePath, DEFAULT_PLAYER_OPTIONS, mode);
    }

    public ProcessAudioPlayback(String audioPlayer, String audioFilePath, int playbackTimeSec) {
        this(audioPlayer, audioFilePath, PlaybackMode.MODE_STOP_AFTER_DELAY);
        this.playbackTimeSec = playbackTimeSec;
    }

    public ProcessAudioPlayback(String audioPlayer, String audioFilePath, String[] options) {
        this(audioPlayer, audioFilePath, options, DEFAULT_PLAYBACK_MODE);
    }

    public ProcessAudioPlayback(String audioPlayer, String audioFilePath, String[] options, PlaybackMode mode) {
        // TODO redirect streams to log
        this.mode = mode;
        this.optionsBeforeFilePath = true;
        this.options = options;
        this.audioFilePath = audioFilePath;

        List<String> command = new ArrayList<>();
        command.add(audioPlayer);
        command.addAll(Arrays.asList(options));
        command.add(audioFilePath);

        playbackProcessBuilder = new ProcessBuilder(command).inheritIO();
        playbackTimeSec = DEFAULT_PLAYBACK_TIME_SEC;
    }

    public boolean isOptionsBeforeFilePath() {
        return optionsBeforeFilePath;
    }

    public void setOptionsBeforeFilePath(boolean optionsBeforeFilePath) {
        if (this.optionsBeforeFilePath != optionsBeforeFilePath) {
            List<String> command = playbackProcessBuilder.command();
            if(optionsBeforeFilePath) {
                // Index 0 - player, Index 1 - audioFilePath
                int indexOfFilePath = 1;
                // Remove filepath from list
                String audioFilePath = command.remove(indexOfFilePath);
                // Put path ath the back
                command.add(audioFilePath);
            } else {
                int firstIndexOfOption = 1;
                // Remove filepath from the back of the list
                String audioFilePath = command.remove(command.size() - 1);
                // Insert the item at the position after the playercommand
                command.add(firstIndexOfOption, audioFilePath);
            }
        }

        this.optionsBeforeFilePath = optionsBeforeFilePath;
    }

    //    "/home/pi/The_Stratosphere_MP3.mp3"

    @Override
    public void play() throws Exception {
        if(audioFilePath != null && !audioFilePath.isEmpty()) {
            if (mode == PlaybackMode.MODE_STOP_ON_RELEASE) {
                process = playbackProcessBuilder.start();
            } else if (mode == PlaybackMode.MODE_STOP_AFTER_DELAY) {
                // Initialize timer lazily
                if (timer == null) {
                    timer = new Timer();
                }

                if (!isPlaying()) {
                    process = playbackProcessBuilder.start();
                    timer.schedule(new TimerTask() {
                        @Override
                        public void run() {
                            process.destroy();
                        }
                    }, playbackTimeSec * 1000);
                }
            }
        }
    }

    @Override
    public void stop() {
        // Terminates the timer
        // if the program terminates before the timer delay has passed
        if(timer != null) {
            timer.cancel();
            timer = null;
        }

        if(isPlaying()) {
            process.destroy();
            process = null;
        }
    }

    public void updateAudio(String newAudioFilePath) throws Exception {
        boolean shouldRestart = false;
        if(isPlaying()) {
            shouldRestart = true;
        }
        // Stop current playback if such is in progress
        stop();

        List<String> command = playbackProcessBuilder.command();
        int audioFilePathIndex = optionsBeforeFilePath? (command.size() - 1) : 1;
        command.set(audioFilePathIndex, newAudioFilePath);

        // Update audioFilePath reference
        this.audioFilePath = newAudioFilePath;

        if (shouldRestart) {
            play();
        }
    }

    @Override
    public void setPlaybackMode(PlaybackMode mode) {
        this.mode = mode;
    }

    @Override
    public PlaybackMode getPlaybackMode() {
        return mode;
    }

    @Override
    public void setPlaybackStopTime(int seconds) {
        playbackTimeSec = seconds;
    }

    @Override
    public boolean isPlaying() {
        if(process == null) {
            return false;
        }

        return process.isAlive();
    }

    @Override
    protected void finalize() {
        this.stop();
    }
}
