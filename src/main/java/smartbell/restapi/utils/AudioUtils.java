package smartbell.restapi.utils;

public class AudioUtils {

    private static final String[] timeFormatterArray = {"00:", "0", "", ":"};

    private static int[] prettifyMilliseconds(long durationMillis) {
        int unFormattedSeconds = Math.round(durationMillis / 1000f);
        int hours = unFormattedSeconds / 3600;
        int unFormattedMinutes = unFormattedSeconds % 3600;
        int minutes = unFormattedMinutes / 60;
        int seconds = unFormattedMinutes % 60;

        return new int[] { seconds, minutes, hours };
    }

    private static String formatTimeDigit(int timeDigit) {
        int digitLength = String.valueOf(timeDigit).length();
        return timeFormatterArray[digitLength] + timeDigit;
    }

    public static String toHumanReadableDuration(String xmpDMDuration) {
        String[] splitDuration = xmpDMDuration.split("\\.");
        if (splitDuration.length != 2) {
            throw new IllegalStateException("Malformed duration string! The string must follow the XMP standard.");
        }

        long durationWholePart = Long.parseLong(splitDuration[0]);
        int[] timeArr = prettifyMilliseconds(durationWholePart);

        int seconds = timeArr[0];
        int minutes = timeArr[1];
        int hours = timeArr[2];
        if(durationWholePart < 60000) { // Case seconds
            return timeFormatterArray[0] + formatTimeDigit(seconds);
        } else if (durationWholePart < 3600000L) { // Case minutes and seconds
           return formatTimeDigit(minutes) + timeFormatterArray[3] + formatTimeDigit(seconds);
        } else if(durationWholePart < 86400000L){ // Case hours minutes and seconds
            return formatTimeDigit(hours) + timeFormatterArray[3] +
                    formatTimeDigit(minutes) + timeFormatterArray[3] +
                    formatTimeDigit(seconds);
        }

        throw new IllegalStateException("Can not format time string! Time string exceeds 24 hours!");
    }
}
