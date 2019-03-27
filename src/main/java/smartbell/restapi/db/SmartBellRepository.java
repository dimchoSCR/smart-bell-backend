package smartbell.restapi.db;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import smartbell.restapi.db.entities.RingEntry;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.List;

@Repository
public class SmartBellRepository {
    private static final String QUERY_GET_ALL_RING_ENTRIES = "SELECT * FROM MAIN.RINGLOG;";
    private static final String QUERY_INSERT_RING = "INSERT INTO MAIN.RINGLOG (RINGTONE_NAME) VALUES (?)";
    private static final String QUERY_GET_ENTRIES_USING_COMPARISON = "SELECT * FROM MAIN.RINGLOG WHERE CREATED_AT <sign> ?";
    private static final String QUERY_REGISTER_APP_INSTANCE = "MERGE INTO MAIN.APPINSTANCE KEY(APP_GUID) VALUES (?, ?)";
    private static final String QUERY_GET_ALL_APP_INSTANCE_TOKENS = "SELECT * FROM MAIN.APPINSTANCE";
    private static final String QUERY_DELETE_APP_INSTANCE_BY_TOKEN = "DELETE FROM MAIN.APPINSTANCE WHERE FIREBASE_TOKEN = ?";

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private RingEntry extractRingEntryFromResultSet(ResultSet resultSet) throws SQLException {

        long id = resultSet.getLong(SmartBellDBContract.RingEntryColumns.COLUMN_ID);
        String melodyName = resultSet.getString(SmartBellDBContract.RingEntryColumns.COLUMN_RINGTONE_NAME);
        OffsetDateTime createdAt = OffsetDateTime.ofInstant(resultSet.getTimestamp(SmartBellDBContract.RingEntryColumns.COLUMN_TIMESTAMP)
                        .toInstant(), ZoneId.systemDefault());

        return new RingEntry(id, melodyName, createdAt);
    }

    // TODO paging
    public List<RingEntry> getAllRingEntries() {
        return jdbcTemplate.query(QUERY_GET_ALL_RING_ENTRIES, (resultSet, i) -> extractRingEntryFromResultSet(resultSet));
    }

    public int addToRingLog(String melodyName) {
        if (melodyName == null) {
            throw new NullPointerException("Melody name cannot be null");
        }

        return jdbcTemplate.update(QUERY_INSERT_RING, melodyName);
    }

    public List<RingEntry> getRingEntriesBasedOn(ComparisonSigns sign, String dateTime) {
        if (sign == null) {
            sign = ComparisonSigns.EQUALS;
        }

        if(dateTime == null) {
            throw new NullPointerException("DateTime parameter must not be null!");
        }

        String queryString = QUERY_GET_ENTRIES_USING_COMPARISON.replace("<sign>", sign.val);
        return jdbcTemplate.query(
                queryString,
                (resultSet, i) -> extractRingEntryFromResultSet(resultSet),
                dateTime
        );
    }

    public int registerAppInstance(String appGUID, String firebaseToken) {
        if (appGUID == null || firebaseToken == null) {
            throw new NullPointerException("AppGUID and FirebaseToken must not be null");
        }

        return jdbcTemplate.update(QUERY_REGISTER_APP_INSTANCE, appGUID, firebaseToken);
    }

    public List<String> getAllAppInstanceTokens() {
        return jdbcTemplate.query(
                QUERY_GET_ALL_APP_INSTANCE_TOKENS,
                (resultSet, i) -> resultSet.getString(SmartBellDBContract.AppInstanceColumns.FIREBASE_TOKEN)
        );
    }

    public int removeAppInstanceByToken(String token) {
        if (token == null) {
            throw new NullPointerException("Token should not be null");
        }

        return jdbcTemplate.update(QUERY_DELETE_APP_INSTANCE_BY_TOKEN, token);
    }
}
