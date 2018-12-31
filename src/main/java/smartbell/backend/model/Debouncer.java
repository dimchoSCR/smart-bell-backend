package smartbell.backend.model;

public class Debouncer {

    private final long delayMillis;
    private final long checkMillis;

    private OnValueChangedListener listener;
    private int cachedValue;
    private int lastBroadcastValue;
    private long stableStateCnt;

    public Debouncer(OnValueChangedListener listener, long delayMillis, long probeMillis) {

        this.listener = listener;
        this.delayMillis = delayMillis;
        this.checkMillis = probeMillis;

        cachedValue = 0;
        lastBroadcastValue = -1;
        stableStateCnt = delayMillis / probeMillis;

    }

    public void debounce(int value) {

        if(cachedValue != value) {
            // Ex. 1000ms / 100ms = 10 (at least 10 stable readings of the pin)
            stableStateCnt = delayMillis / checkMillis;
            cachedValue = value;
        } else {
            if(--stableStateCnt == 0) {
                // Broadcast value only if it has changed during the debounce period
                if(lastBroadcastValue != value) {
                    listener.onValueChanged(value);
                }

                lastBroadcastValue = value;
                // Reset the stableStateCnt
                stableStateCnt = delayMillis / checkMillis;
            }
        }

    }

}
