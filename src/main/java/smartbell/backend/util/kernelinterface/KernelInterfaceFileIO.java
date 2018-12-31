package smartbell.backend.util.kernelinterface;

import smartbell.backend.util.PinAttributes;

import java.io.*;

public class KernelInterfaceFileIO {
    private static final String SYS_GPIO_DRIVER_DIR_PATH = "/sys/class/gpio/";
    private static final String SYS_PIN_DIR_PREFIX = "gpio";

    public static final String SYS_GPIO_EXPORT_FILE_PATH = SYS_GPIO_DRIVER_DIR_PATH + "export";
    public static final String SYS_GPIO_UNEXPORT_FILE_PATH = SYS_GPIO_DRIVER_DIR_PATH + "unexport";

    public static String getPinAttributePath(String pinNumber, PinAttributes attr) {
        return SYS_GPIO_DRIVER_DIR_PATH + // Base folder for the gpio pins
                SYS_PIN_DIR_PREFIX + // The "gpio" prefix in the gpio<pin> folder name
                pinNumber + // The pin number e.g. 1
                File.separatorChar +
                attr.value; // A pin attribute (direction, value)
    }

    public static FileWriter getFileWriterFor(String path) {
        File kernelInterfaceFile = new File(path);

        if(!kernelInterfaceFile.exists()) {
            if(path.equals(SYS_GPIO_EXPORT_FILE_PATH) || path.equals(SYS_GPIO_UNEXPORT_FILE_PATH)) {
                throw new IllegalStateException("GPIO kernel driver files not available!");
            } else {
                throw new IllegalStateException("The specified pin is not exported! Did you forget to call export.");
            }
        }

        if(!kernelInterfaceFile.canWrite()) {
            throw new IllegalStateException("No permission to write in kernel interface files!");
        }

        try {
            return new FileWriter(kernelInterfaceFile);
        } catch (IOException e) {
            // FileWriter() throws an IOException if the specified gpio file does not exist
            // Check for the gpio files is performed above
            throw new IllegalStateException("Unknown GPIO Kernel interface state!");
        }
    }

    public static RandomAccessFile getRandomFileReaderFor(String path) {
        File kernelInterfaceFile = new File(path);
        if(!kernelInterfaceFile.exists()) {
            throw new IllegalStateException("The specified pin is not exported! Did you forget to call export.");
        }

        try {
            return new RandomAccessFile(kernelInterfaceFile, "r");
        } catch (FileNotFoundException e) {
            // RandomAccessFile() throws an FileNotFoundException if the specified gpio file does not exist
            // Check for the gpio files is performed above
            throw new IllegalStateException("Unknown GPIO Kernel interface state!");
        }
    }

    public static synchronized void writeWith(FileWriter writer, String value) throws IOException {
        try {
            writer.write(value);
            writer.flush();
        } catch (IOException e) {
            throw new IOException("Could not write to kernel interface file!", e);
        }
    }

    public static String readWith(RandomAccessFile reader) throws IOException {
        try {
            return reader.readLine();
        } catch (IOException e) {
            throw new IOException("Could not read from kernel interface file!", e);
        }
    }
}
