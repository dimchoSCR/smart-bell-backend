package smartbell.restapi.db;

public class SmartBellDBContract {
    public static class RingEntryColumns {
        public static final String COLUMN_ID = "id";
        public static final String COLUMN_RINGTONE_NAME = "ringtone_name";
        public static final String COLUMN_TIMESTAMP = "created_at";
    }

    public static class AppInstanceColumns {
        public static final String APP_GUID = "app_guid";
        public static final String FIREBASE_TOKEN = "firebase_token";
    }
}
