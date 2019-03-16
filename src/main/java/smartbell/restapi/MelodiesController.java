package smartbell.restapi;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import smartbell.restapi.db.ComparisonSigns;
import smartbell.restapi.db.SmartBellRepository;
import smartbell.restapi.db.entities.RingEntry;
import smartbell.restapi.donotdisturb.DoNotDisturbManager;
import smartbell.restapi.log.RingLogManager;
import smartbell.restapi.melody.MelodyInfo;
import smartbell.restapi.melody.MelodyManager;

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

    @GetMapping
    public List<MelodyInfo> getAllMelodies() {
        return melodyManager.listMelodies();
    }

    @GetMapping("/log")
    public List<RingEntry> getFullLog(@QueryParam("compSign") ComparisonSigns compSign,
                                      @QueryParam("timeString") String timeString) {

       return ringLogManager.getAllRingLogEntries(compSign, timeString);
    }

    @PostMapping("/upload")
    public void uploadMusicFile(@RequestParam("file") MultipartFile musicFile) {
        melodyManager.addToMelodieLibrary(musicFile);
        System.out.println("File uploaded successfully");
    }

    @PutMapping("/update/ringtone")
    public String setRingtone(@RequestBody String melodyName) {
        return melodyManager.setAsRingtone(melodyName);
    }

    @GetMapping("/donotdisturb/enable")
    public String enableDoNotDisturb() {
        doNotDisturbManager.enableDoNotDisturbMode();
        return "Success";
    }

    @PutMapping("/donotdisturb/rules")
    public String enableDoNotDisturbWithRules(
            @RequestParam("startTime") String startTime,
            @RequestParam("endTime") String endTime,
            @RequestParam boolean endTomorrow
    ) {

        doNotDisturbManager.scheduleDoNotDisturb(startTime, endTime, endTomorrow);
        return null;
    }

    @GetMapping("/donotdisturb/disable")
    public String disableDoNotDisturb() {
        doNotDisturbManager.disableDoNotDisturbMode();
        return "Success";
    }

    @InitBinder
    public void initBinder(final WebDataBinder webdataBinder) {
        webdataBinder.registerCustomEditor(ComparisonSigns.class, new SignToEnumConverter());
    }
}
