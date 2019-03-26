package smartbell.restapi.donotdisturb;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class DoNotDisturbUtil {

    private static final Map<String, Integer> weekDayNameToNumber = new HashMap<>();
    static {
        weekDayNameToNumber.put("Mon", 0);
        weekDayNameToNumber.put("Tue", 1);
        weekDayNameToNumber.put("Wed", 2);
        weekDayNameToNumber.put("Thu", 3);
        weekDayNameToNumber.put("Fri", 4);
        weekDayNameToNumber.put("Sat", 5);
        weekDayNameToNumber.put("Sun", 6);
    }

    public static boolean isDoNotDisturbScheduledForToday(int[] days) {
        String weekDayName = new SimpleDateFormat("EEE").format(Calendar.getInstance().getTime());
        for (int day : days) {
            if (day == weekDayNameToNumber.get(weekDayName)) {
                return true;
            }
        }

        return false;
    }
}
