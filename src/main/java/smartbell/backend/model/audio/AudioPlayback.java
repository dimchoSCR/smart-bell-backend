package smartbell.backend.model.audio;

public interface AudioPlayback {
    void play() throws Exception;
    void stop();
    void setPlaybackMode(PlaybackMode mode);
    PlaybackMode getPlaybackMode();
    void setPlaybackStopTime(int seconds);
    boolean isPlaying();
}
