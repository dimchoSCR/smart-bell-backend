package smartbell.backend.model.audio;

public interface AudioPlayback {
    void play() throws Exception;
    void stop();
    void setOnStopListener(Runnable onAfterStop);
    void removeOnStopListener();
    void setPlaybackMode(PlaybackMode mode);
    String getCurrentSongName();
    PlaybackMode getPlaybackMode();
    void setPlaybackStopTime(int seconds);
    boolean isPlaying();
}
