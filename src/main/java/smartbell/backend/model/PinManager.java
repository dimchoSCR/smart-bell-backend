package smartbell.backend.model;

public interface PinManager {
    Pin exportPin(PinNumberSystem pinNumberSystem) throws Exception;
    void unexportPin(Pin pinNumberSystem) throws Exception;
    Pin provisionPin(PinNumberSystem pinNumberSystem, Pin.Direction direction) throws Exception;
    void resetPins();
}
