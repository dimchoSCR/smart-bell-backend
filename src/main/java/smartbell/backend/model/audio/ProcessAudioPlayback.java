package smartbell.backend.model.audio;

import java.util.Timer;
import java.util.TimerTask;

public class ProcessAudioPlayback implements AudioPlayback {
    private static final int DEFAULT_PLAYBACK_TIME_SEC = 5;

    public static final PlaybackMode DEFAULT_PLAYBACK_MODE = PlaybackMode.MODE_STOP_ON_RELEASE;
    public static final String DEFAULT_AUDIO_PLAYER = "mpg123";

    private final ProcessBuilder playbackProcessBuilder;
    private PlaybackMode mode;
    private Timer timer;
    private Process process;

    private int playbackTimeSec;

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
        this(audioPlayer, audioFilePath, "", mode);
    }

    public ProcessAudioPlayback(String audioPlayer, String audioFilePath, int playbackTimeSec) {
        this(audioPlayer, audioFilePath, PlaybackMode.MODE_STOP_AFTER_DELAY);
        this.playbackTimeSec = playbackTimeSec;
    }

    public ProcessAudioPlayback(String audioPlayer, String audioFilePath, String options) {
        this(audioPlayer, audioFilePath, options, DEFAULT_PLAYBACK_MODE);
    }

    public ProcessAudioPlayback(String audioPlayer, String audioFilePath, String options, PlaybackMode mode) {
        // TODO redirect streams to log
        this.mode = mode;

        playbackProcessBuilder = new ProcessBuilder(audioPlayer, audioFilePath, options).inheritIO();
        playbackTimeSec = DEFAULT_PLAYBACK_TIME_SEC;
    }

//    "/home/pi/The_Stratosphere_MP3.mp3"

    @Override
    public void play() throws Exception {
        if(mode == PlaybackMode.MODE_STOP_ON_RELEASE) {
            process = playbackProcessBuilder.start();
        } else if(mode == PlaybackMode.MODE_STOP_AFTER_DELAY) {
            // Initialize timer lazily
            if(timer == null) {
                timer = new Timer();
            }

            if(!isPlaying()) {
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
