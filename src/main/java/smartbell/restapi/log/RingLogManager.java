package smartbell.restapi.log;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import smartbell.restapi.exceptions.BellServiceException;
import smartbell.restapi.db.ComparisonSigns;
import smartbell.restapi.db.SmartBellRepository;
import smartbell.restapi.db.entities.RingEntry;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Component
public class RingLogManager {

    @Autowired
    private SmartBellRepository bellRepository;

    public List<RingEntry> getAllRingLogEntries(ComparisonSigns compSign, String timeStringUTC) {
        if (compSign == null && timeStringUTC == null) {
            return bellRepository.getAllRingEntries();
        }

        return bellRepository.getRingEntriesBasedOn(compSign, timeStringUTC);
    }

    public void addRingToLog(String playingMelodyName) {
        int affectedRows = bellRepository.addToRingLog(playingMelodyName);

        if(affectedRows == 0 || affectedRows > 1) {
            throw new BellServiceException("Bad affected rows count: " + affectedRows);
        }
    }

    public void addToRingLogAsync(String playingMelodyName) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> addRingToLog(playingMelodyName));
        executor.shutdown();
    }
}
