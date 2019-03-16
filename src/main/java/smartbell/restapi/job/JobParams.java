package smartbell.restapi.job;

import java.util.HashMap;
import java.util.Map;

public class JobParams {
    private final Map<String, Object> paramsMap;


    public JobParams() {
        paramsMap = new HashMap<>();
    }

    public void putLong(String key, long value) {
        paramsMap.put(key, value);
    }

    public long getLong(String key, long defaultValue) {
        Object el = paramsMap.get(key);
        if (el == null) {
            return defaultValue;
        }

        return (long) el;
    }

    public void putString(String key, String value) {
        paramsMap.put(key, value);
    }

    public String getString(String key, String defaultValue) {
        Object el = paramsMap.get(key);
        if (el == null) {
            return defaultValue;
        }

        return (String) el;
    }
}
