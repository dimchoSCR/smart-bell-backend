package smartbell.backend.model;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public enum GPIO implements PinNumberSystem {
    PIN_0("0"), PIN_1("1"), PIN_2("2"), PIN_27("27");

    private static final Map<String, String> wPiToBcmMapping;
    static {
        Map<String, String> backingMap = new HashMap<>();
        backingMap.put("0", "17");
        backingMap.put("1", "18");
        backingMap.put("2", "27");
        backingMap.put("27", "16");

        wPiToBcmMapping = Collections.unmodifiableMap(backingMap);
    }

    private String value;

    GPIO(String value) {
        this.value = value;
    }

    @Override
    public String getWPiPinNumber() {
        return this.value;
    }

    @Override
    public String getBCMPinNumber() {
        return wPiToBcmMapping.get(this.value);
    }
}
