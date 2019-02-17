package smartbell.restapi.db;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import smartbell.restapi.BellServiceException;
import smartbell.restapi.db.entities.RingEntry;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public class SmartBellRepository {
    private static final String QUERY_GET_ALL = "SELECT * FROM MAIN.RINGLOG;";
    private static final String QUERY_INSERT_RING = "INSERT INTO MAIN.RINGLOG (RINGTONE_NAME) VALUES (?)";
    private static final String QUERY_GET_ENTRIES_USING_COMPARISON = "SELECT * FROM MAIN.RINGLOG WHERE CREATED_AT <sign> ?";

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private RingEntry extractRingEntryFromResultSet(ResultSet resultSet) throws SQLException {

        long id = resultSet.getLong(SmartBellDBContract.RingEntryColumns.COLUMN_ID);
        String melodyName = resultSet.getString(SmartBellDBContract.RingEntryColumns.COLUMN_RINGTONE_NAME);
        LocalDateTime createdAt = resultSet.getTimestamp(SmartBellDBContract.RingEntryColumns.COLUMN_TIMESTAMP)
                        .toLocalDateTime();

        return new RingEntry(id, melodyName, createdAt);
    }

    // TODO paging
    public List<RingEntry> getAllEntries() {
        return jdbcTemplate.query(QUERY_GET_ALL, (resultSet, i) -> extractRingEntryFromResultSet(resultSet));
    }

    public int addToRingLog(String melodyName) {
        if(melodyName == null) {
            throw new NullPointerException("Melody name cannot be null");
        }

        return jdbcTemplate.update(QUERY_INSERT_RING, melodyName);
    }

    public List<RingEntry> getEntriesBasedOn(ComparisonSigns sign, String dateTime) {
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
}
