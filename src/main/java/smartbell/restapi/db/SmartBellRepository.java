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

    @Autowired
    JdbcTemplate jdbcTemplate;

    // TODO paging
    public List<RingEntry> getAllEntries() {
        return jdbcTemplate.query(QUERY_GET_ALL, (resultSet, i) -> {
            RingEntry ringEntry = new RingEntry();

            String melodyName = resultSet.getString(SmartBellDBContract.RingEntryColumns.COLUMN_RINGTONE_NAME);
            LocalDateTime createdAt =
                    resultSet.getTimestamp(SmartBellDBContract.RingEntryColumns.COLUMN_TIMESTAMP)
                    .toLocalDateTime();

            ringEntry.setMelodyName(melodyName);
            ringEntry.setDateTime(createdAt);

            return ringEntry;
        });
    }
}
