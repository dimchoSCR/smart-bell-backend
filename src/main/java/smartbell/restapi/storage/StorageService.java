package smartbell.restapi.storage;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

public interface StorageService {
    String constructPathStringUsing(String dir, String... more);
    void createDirectory(String rootPath, String... more) throws IOException;
    void store(String directory, MultipartFile file) throws IOException;
    boolean isFile(String dir, String... more);
    Resource getAsResource(String filePath);
    void link(String filePath, String destinationDir) throws IOException;
    void unlink(String linkPath) throws IOException;
    Stream<Path> listAll(String directory) throws IOException;
    Path listOnly(String directory) throws Exception;
    void delete(String filePath) throws IOException;
    void deleteAll(String directory);
}