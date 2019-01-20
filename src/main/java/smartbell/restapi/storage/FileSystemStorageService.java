package smartbell.restapi.storage;

import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.*;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class FileSystemStorageService implements StorageService {
    private static final String SYM_LINK_PREFIX = "LinkTo:";

    @Override
    public String constructPathStringUsing(String dir, String... more) throws IOException {
        Path filePath = Paths.get(dir, more);

        if(!Files.exists(filePath)) {
            throw new FileNotFoundException("The path strings you provided do not denote an existing file!");
        }

        if(Files.isDirectory(filePath)) {
            throw new FileNotFoundException("The path strings you provided denote a directory!");
        }

        return filePath.toString();
    }

    @Override
    public void createDirectory(String rootPath, String... more) throws IOException {
        Path dirPath = Paths.get(rootPath, more);
        if (!Files.isDirectory(dirPath, LinkOption.NOFOLLOW_LINKS)) {
            Files.createDirectories(dirPath);
        }
    }

    @Override
    public void store(String directory, MultipartFile file) throws IOException {
        Path dirPath = Paths.get(directory);
        if (!Files.isDirectory(dirPath)) {
            throw new FileNotFoundException("The directory " + directory + " does not exist!");
        }

        Path filePath = dirPath.resolve(file.getOriginalFilename());
        try(InputStream is = file.getInputStream()) {
            Files.copy(is, filePath, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    @Override
    public boolean isFile(String dir, String... more) {
        Path pathToFile = Paths.get(dir, more);

        return Files.exists(pathToFile) && !Files.isDirectory(pathToFile);
    }

    @Override
    public Stream<Path> listAll(String directory) throws IOException{
        Path dirPath = Paths.get(directory);
        if (!Files.isDirectory(dirPath)) {
            throw new FileNotFoundException("The directory you are trying to list does not exists!");
        }

        return Files.list(dirPath);
//        return files.map(file -> new FileInfo(file.getFileName().toString()))
//                .collect(Collectors.toList());
    }

    @Override
    public Path listOnly(String directory) throws Exception {
        List<Path> files = listAll(directory).collect(Collectors.toList());
        if(files.isEmpty()) {
            return null;
        }

        if(files.size() > 1) {
            throw new Exception("Too many files in directory!");
        }

        return files.get(0);
    }

    public String getPathToOnlyFileInDir(String directory) throws Exception {
        if(listOnly(directory) == null) {
            return null;
        }

        return listOnly(directory).toAbsolutePath().toString();
    }

    @Override
    public Resource getAsResource(String filePath) {
        return null;
    }

    @Override
    public void link(String filePath, String destinationDir) throws IOException {
        Path fileToLinkPath = Paths.get(filePath);
        if(!Files.exists(fileToLinkPath)) {
            throw new FileNotFoundException("The file you are trying to link does not exist!");
        }

        Path destinationDirPath = Paths.get(destinationDir);
        if(!Files.isDirectory(destinationDirPath)) {
            throw new FileNotFoundException("The directory you are trying to link to does not exist!");
        }

        Path destinationLinkPath = destinationDirPath.resolve(fileToLinkPath.getFileName());
        Files.createSymbolicLink(destinationLinkPath, fileToLinkPath);
    }

    @Override
    public void unlink(String linkFilePath) throws IOException {
        if (!Files.isSymbolicLink(Paths.get(linkFilePath))) {
            throw new IOException("The file you are trying to unlink is not a link!");
        }

        delete(linkFilePath);
    }

    @Override
    public void delete(String filePath) throws IOException {
        Files.deleteIfExists(Paths.get(filePath));
    }

    @Override
    public void deleteAll(String directory) {

    }

}
