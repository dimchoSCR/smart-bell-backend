package smartbell.restapi;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import smartbell.restapi.db.ComparisonSigns;
import smartbell.restapi.db.entities.RingEntry;
import smartbell.restapi.status.DoNotDisturbManager;
import smartbell.restapi.log.RingLogManager;
import smartbell.restapi.melody.MelodyInfo;
import smartbell.restapi.melody.MelodyManager;
import smartbell.restapi.status.BellStatus;
import smartbell.restapi.status.DoNotDisturbStatus;

import javax.ws.rs.QueryParam;
import java.util.List;

@RestController
@RequestMapping(value = "/melodies")
public class MelodiesController {

    @Autowired
    private MelodyManager melodyManager;
    @Autowired
    private RingLogManager ringLogManager;
    @Autowired
    private DoNotDisturbManager doNotDisturbManager;

    @PostMapping("/upload")
    public void uploadMusicFile(@RequestParam("file") MultipartFile musicFile) {
        melodyManager.addToMelodieLibrary(musicFile);
    }

    @PutMapping("/update/ringtone")
    public String setRingtone(@RequestBody String melodyName) {
        return melodyManager.setAsRingtone(melodyName);
    }

    @PutMapping("/delete")
    public void deleteMelody(@RequestParam("melodyName") String melodyName) {
        melodyManager.deleteMelody(melodyName);
    }

    @GetMapping
    public List<MelodyInfo> getAllMelodies() {
        return melodyManager.listMelodies();
    }

    @GetMapping("/log")
    public List<RingEntry> getFullLog(@QueryParam("compSign") ComparisonSigns compSign,
                                      @QueryParam("timeString") String timeString) {

        return ringLogManager.getAllRingLogEntries(compSign, timeString);
    }

    @GetMapping("/donotdisturb/enable")
    public String enableDoNotDisturb() {
        doNotDisturbManager.enableDoNotDisturbMode();
        return "Success";
    }

    @PutMapping("/donotdisturb/rules")
    public void enableDoNotDisturbWithRules(
            @RequestParam("days") int[] days,
            @RequestParam("startTime") long startTime,
            @RequestParam("endTime") long endTime,
            @RequestParam("endTomorrow") boolean endTomorrow
    ) {

        doNotDisturbManager.scheduleDoNotDisturb(days, startTime, endTime, endTomorrow);
    }

    @GetMapping("/donotdisturb/disable")
    public String disableDoNotDisturb() {
        doNotDisturbManager.disableDoNotDisturbMode();
        return "Success";
    }

    @GetMapping("/donotdisturb/status")
    public DoNotDisturbStatus getDoNotDisturbStatus() {
        return doNotDisturbManager.getDisturbStatus();
    }

    @GetMapping("/status")
    public BellStatus getBellStatus() {
        return melodyManager.getBellStatus();
    }

    @PutMapping("/status/playbackMode")
    public void setPlaybackMode(@RequestParam("playbackMode") String playbackMode) {
        melodyManager.setBellPlaybackMode(playbackMode);
    }

    @PutMapping("/status/playbackDuration")
    public void setPlaybackDuration(@RequestParam("playbackDuration") int playbackDuration) {
        melodyManager.setBellPlaybackDuration(playbackDuration);
    }

    @PutMapping("/status/ringVolume")
    public void setRingVolume(@RequestParam("ringVolume") int ringVolume) {
        melodyManager.setBellVolume(ringVolume);
    }

    @PutMapping("/preplay/start")
    public void startMelodyPrePlay(@RequestParam("melodyName") String melodyName) {
        melodyManager.startMelodyPrePlay(melodyName);
    }

    @PutMapping("/preplay/end")
    public void endMelodyPrePlay() {
        melodyManager.endMelodyPrePlay();
    }

    @InitBinder
    public void initBinder(final WebDataBinder webdataBinder) {
        webdataBinder.registerCustomEditor(ComparisonSigns.class, new SignToEnumConverter());
    }
}
