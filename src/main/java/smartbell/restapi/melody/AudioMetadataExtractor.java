package smartbell.restapi.melody;

import org.apache.tika.metadata.Metadata;
import org.apache.tika.metadata.XMPDM;
import org.apache.tika.mime.MediaType;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.audio.AudioParser;
import org.apache.tika.parser.audio.MidiParser;
import org.apache.tika.parser.mp3.Mp3Parser;
import org.apache.tika.parser.mp4.MP4Parser;
import org.apache.tika.sax.BodyContentHandler;
import org.gagravarr.tika.FlacParser;
import org.gagravarr.tika.VorbisParser;
import org.springframework.stereotype.Component;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

@Component
public class AudioMetadataExtractor {
    static final String AUDIO_BASE_CONTENT_TYPE = "audio";
    static final String META_CONTENT_TYPE = "Content-Type";
    static final String OGG_CONTENT_TYPE = "audio/vorbis";

    private final AutoDetectParser parser = new AutoDetectParser(
            new AudioParser(),
            new MidiParser(),
            new Mp3Parser(),
            new VorbisParser(),
            new FlacParser(),
            new MP4Parser()
    );

    private final ParseContext parseCtx = new ParseContext();

    private String tryExtractingDurationUsingAudioStream(String melodyPath) throws IOException {
        AudioInputStream audioInputStream = null;
        try {
            audioInputStream = AudioSystem.getAudioInputStream(new File(melodyPath));
        } catch (UnsupportedAudioFileException e) {
            return null;
        }

        AudioFormat format = audioInputStream.getFormat();
        long frames = audioInputStream.getFrameLength();

        return String.valueOf((frames / format.getFrameRate()) * 1000);
    }

    Metadata parseAudioFileMetadata(String audioFilePath) throws Exception {

        try (InputStream stream = new FileInputStream(audioFilePath)) {
            BodyContentHandler handler = new BodyContentHandler();
            Metadata metadata = new Metadata();
            parser.parse(stream, handler, metadata, parseCtx);

            if(!metadata.get("Content-Type").contains("audio/")) {
                throw new Exception("The specified file is not an audio file!");
            }

            return metadata;
        }

    }

    MediaType detectContentType(InputStream audioInputStream) throws IOException {
        return parser.getDetector().detect(audioInputStream, new Metadata());
    }

    String tryExtractingDuration(Metadata melodyMetadata, String melodyPath) throws IOException {
        String duration = melodyMetadata.get(XMPDM.DURATION);
        if(duration != null) {
            String audioContentType = melodyMetadata.get(AudioMetadataExtractor.META_CONTENT_TYPE);
            // For some reason apache tika parses the xmpDMDuration in seconds for ogg and in milliseconds for mp3
            if(audioContentType.equalsIgnoreCase(OGG_CONTENT_TYPE)) {
                double durationMillis = Double.parseDouble(duration) * 1000;
                return String.valueOf(durationMillis);
            }

            return duration;
        }

        return tryExtractingDurationUsingAudioStream(melodyPath);
    }
}
