package smartbell.restapi.status;


import smartbell.backend.model.audio.PlaybackMode;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

public class CoreStatus {

    public static final String RINGTONE_PROPERTY_NAME = "currentRingtone";
    public static final String PLAYBACK_MODE_PROPERTY_NAME = "playbackMode";
    public static final String RING_VOLUME_PROPERTY_NAME = "ringVolume";
    public static final String PLAYBACK_TIME_PROPERTY_NAME = "playbackTime";

    private String currentRingtone;
    private String playbackMode;

    private int ringVolume;
    private int playbackTime;

    private PropertyChangeSupport support;

    CoreStatus() {
        // TODO apply default value from server preference
        playbackMode = PlaybackMode.MODE_STOP_AFTER_DELAY.name();
        ringVolume = 70;
        playbackTime = 30;

        support = new PropertyChangeSupport(this);
    }

    public void addPropertyChangeListener(PropertyChangeListener pcl) {
        support.addPropertyChangeListener(pcl);
    }

    public void removePropertyChangeListener(PropertyChangeListener pcl) {
        support.removePropertyChangeListener(pcl);
    }

    private void notifyCoreStatusChanged(String name, Object oldValue, Object newVale) {
       support.firePropertyChange(name, oldValue, newVale);
    }

    public String getCurrentRingtone() {
        return currentRingtone;
    }

    public void setCurrentRingtone(String currentRingtone) {
        String oldValue = this.currentRingtone;
        this.currentRingtone = currentRingtone;
        notifyCoreStatusChanged(RINGTONE_PROPERTY_NAME, oldValue, currentRingtone);
    }

    public String getPlaybackMode() {
        return playbackMode;
    }

    public void setPlaybackMode(String playbackMode) {
        String oldValue = this.playbackMode;
        this.playbackMode = playbackMode;
        notifyCoreStatusChanged(PLAYBACK_MODE_PROPERTY_NAME, oldValue, playbackMode);
    }

    public int getRingVolume() {
        return ringVolume;
    }

    public void setRingVolume(int ringVolume) {
        int oldValue = this.ringVolume;
        this.ringVolume = ringVolume;
        notifyCoreStatusChanged(RING_VOLUME_PROPERTY_NAME, oldValue, ringVolume);
    }

    public int getPlaybackTime() {
        return playbackTime;
    }

    public void setPlaybackTime(int playbackTime) {
        int oldValue = this.playbackTime;
        this.playbackTime = playbackTime;
        notifyCoreStatusChanged(PLAYBACK_TIME_PROPERTY_NAME, oldValue, playbackTime);
    }
}
