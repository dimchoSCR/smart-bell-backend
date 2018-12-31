package smartbell.restapi.storage;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface StorageService {
    String constructPathStringUsing(String dir, String... more);
    void createDirectory(String rootPath, String... more) throws IOException;
    void store(String directory, MultipartFile file) throws IOException;
    boolean isFile(String dir, String... more);
    Resource getAsResource(String filePath);
    void link(String filePath, String destinationDir) throws IOException;
    List<String> listAll(String directory);
    void delete(String filePath);
    void deleteAll(String directory);
}