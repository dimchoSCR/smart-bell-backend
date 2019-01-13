package smartbell.restapi.melody;

public class MelodyInfo {

    private String melodyName;
    // TODO file size
    private int fileSize;
    // TODO duration
    private String duration;

    private boolean isRingtone;

    public MelodyInfo(String melodyName, long fileSize, String duration, boolean isRingtone) {
        this.melodyName = melodyName;
        this.fileSize = (int) fileSize;
        this.duration = duration;
        this.isRingtone = isRingtone;
    }

    public String getMelodyName() {
        return melodyName;
    }

    public void setMelodyName(String melodyName) {
        this.melodyName = melodyName;
    }

    public int getFileSize() {
        return fileSize;
    }

    public void setFileSize(int fileSize) {
        this.fileSize = fileSize;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public boolean isRingtone() {
        return isRingtone;
    }

    public void setRingtone(boolean ringtone) {
        isRingtone = ringtone;
    }

}
