package smartbell.restapi.melody;

import smartbell.restapi.utils.AudioUtils;
import smartbell.restapi.utils.FileSizeUtil;

public class MelodyInfo {

    private String melodyName;
    private String fileSize;
    private String duration;
    private String contentType;

    private boolean isRingtone;

    public MelodyInfo(String melodyName, String contentType, long fileSize, String duration, boolean isRingtone) {
        this.melodyName = melodyName;
        this.contentType = contentType;
        this.fileSize = FileSizeUtil.toHumanReadableSize(fileSize);
        this.duration = AudioUtils.toHumanReadableDuration(duration, contentType);
        this.isRingtone = isRingtone;
    }

    public String getMelodyName() {
        return melodyName;
    }

    public void setMelodyName(String melodyName) {
        this.melodyName = melodyName;
    }

    public String getFileSize() {
        return fileSize;
    }

    public void setFileSize(String fileSize) {
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
