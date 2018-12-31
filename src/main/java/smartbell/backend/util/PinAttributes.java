package smartbell.backend.util;

public enum PinAttributes {
    SYS_PIN_ATTRIBUTE_DIRECTION("direction"), SYS_PIN_ATTRIBUTE_VALUE("value");

    public String value;
    PinAttributes(String value) {
        this.value = value;
    }
}
