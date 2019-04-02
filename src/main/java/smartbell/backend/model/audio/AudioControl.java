package smartbell.backend.model.audio;

import java.io.IOException;

public interface AudioControl {
    void setVolumeLevel(int percent) throws IOException;
}
