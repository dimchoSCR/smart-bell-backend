package smartbell.backend.model;

public abstract class Pin {
    private static final Direction DEFAULT_DIRECTION = Direction.IN;

    protected String pinNumber;
    protected Direction cachedDirection;
    protected long debounceMillis;

    public Pin(String pinNumber) {
        this.pinNumber = pinNumber;
        this.debounceMillis = 0L;
        this.cachedDirection = DEFAULT_DIRECTION;
    }

    public abstract void setDirection(Direction direction) throws Exception;
    public abstract void setValue(Value value) throws Exception;
    public abstract int getValue() throws Exception;
    public abstract void setOnValueChangedListener(OnValueChangedListener listener);
    public abstract void removeOnValueChangedListener();

    public String getNumber() {
        return pinNumber;
    }
    public void setDebounce(long millis) {
        if(cachedDirection != Direction.IN) {
            throw new IllegalStateException("Debounce is only applicable to output pin only!");
        }

        this.debounceMillis = millis;
    }

    public enum Direction {
        IN("in"), OUT("out"), LOW("low"), HIGH("high");

        public String value;
        Direction(String value) {
            this.value = value;
        }
    }

    public enum Value {
       LOW("0"), HIGH("1");

        public String value;
        Value(String value) {
            this.value = value;
        }
    }
}
