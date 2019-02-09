package smartbell.restapi.melody;

public class MelodyInfo {

    private String melodyName;
    private String duration;
    private String contentType;

    private long fileSize;
    private boolean isRingtone;

    public MelodyInfo(String melodyName, String contentType, long fileSize, String duration, boolean isRingtone) {
        this.melodyName = melodyName;
        this.contentType = contentType;
        this.fileSize = fileSize;
        this.duration = duration;
        this.isRingtone = isRingtone;
    }

    public String getMelodyName() {
        return melodyName;
    }

    public void setMelodyName(String melodyName) {
        this.melodyName = melodyName;
    }

    public long getFileSize() {
        return fileSize;
    }

    public void setFileSize(long fileSize) {
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

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }
}
