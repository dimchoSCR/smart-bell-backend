package smartbell.restapi.db;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import smartbell.restapi.db.entities.RingEntry;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public class SmartBellRepository {
    private static final String QUERY_GET_ALL = "SELECT * FROM MAIN.RINGLOG;";
    private static final String QUERY_INSERT_RING = "INSERT INTO MAIN.RINGLOG (RINGTONE_NAME) VALUES (?)";

    @Autowired
    JdbcTemplate jdbcTemplate;

    // TODO paging
    public List<RingEntry> getAllEntries() {
        return jdbcTemplate.query(QUERY_GET_ALL, (resultSet, i) -> {
            RingEntry ringEntry = new RingEntry();

            long id = resultSet.getLong(SmartBellDBContract.RingEntryColumns.COLUMN_ID);
            String melodyName = resultSet.getString(SmartBellDBContract.RingEntryColumns.COLUMN_RINGTONE_NAME);
            LocalDateTime createdAt =
                    resultSet.getTimestamp(SmartBellDBContract.RingEntryColumns.COLUMN_TIMESTAMP)
                    .toLocalDateTime();

            ringEntry.setId(id);
            ringEntry.setMelodyName(melodyName);
            ringEntry.setDateTime(createdAt);

            return ringEntry;
        });
    }

    public int addToRingLog(String melodyName) {
        if(melodyName == null) {
            throw new NullPointerException("Melody name cannot be null");
        }

        return jdbcTemplate.update(QUERY_INSERT_RING, melodyName);
    }
}
