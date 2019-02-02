package smartbell.restapi.melody;

import org.apache.tika.metadata.Metadata;
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

    private final AutoDetectParser parser = new AutoDetectParser(
            new AudioParser(),
            new MidiParser(),
            new Mp3Parser(),
            new VorbisParser(),
            new FlacParser(),
            new MP4Parser()
    );

    private final ParseContext parseCtx = new ParseContext();

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

    String tryExtractingDurationFromAudioStream(String melodyPath) throws IOException {
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
}
