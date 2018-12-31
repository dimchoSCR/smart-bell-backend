package smartbell.restapi;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import smartbell.restapi.melody.MelodyManager;

@RestController
@RequestMapping(value = "/melodies")
public class MelodiesController {

    @Autowired
    private MelodyManager melodyManager;

    @GetMapping()
    public String hello() {
        return "Hello restapi!";
    }

    @PostMapping("/upload")
    public void uploadMusicFile(@RequestParam("file") MultipartFile musicFile) {
        melodyManager.addToMelodieLibrary(musicFile);
        System.out.println("File uploaded successfully");
    }

    @PutMapping("/update/ringtone")
    public String setRingtone(@RequestParam("name") String melodyName) {
        return melodyManager.setAsRingtone(melodyName);
    }
}
