package smartbell.backend.model.audio;

public enum AudioChannel {
    MASTER("Master"), PCM("PCM");

    String value;
    AudioChannel(String value) {
        this.value = value;
    }
}
