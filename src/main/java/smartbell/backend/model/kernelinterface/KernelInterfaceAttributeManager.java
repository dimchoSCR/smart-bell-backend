package smartbell.backend.model.kernelinterface;

import smartbell.backend.model.Pin;
import smartbell.backend.util.kernelinterface.KernelInterfaceFileIO;
import smartbell.backend.util.PinAttributes;

import java.io.FileWriter;
import java.io.IOException;
import java.io.RandomAccessFile;

class KernelInterfaceAttributeManager {
    private final FileWriter directionFileWriter;
    private final FileWriter valueFileWriter;

    private final RandomAccessFile valueRandomFileReader;

    KernelInterfaceAttributeManager(String pinNumber) {
        directionFileWriter = KernelInterfaceFileIO.getFileWriterFor(
                KernelInterfaceFileIO.getPinAttributePath(pinNumber, PinAttributes.SYS_PIN_ATTRIBUTE_DIRECTION)
        );

        valueFileWriter = KernelInterfaceFileIO.getFileWriterFor(
                KernelInterfaceFileIO.getPinAttributePath(pinNumber, PinAttributes.SYS_PIN_ATTRIBUTE_VALUE)
        );

        valueRandomFileReader = KernelInterfaceFileIO.getRandomFileReaderFor(
                KernelInterfaceFileIO.getPinAttributePath(pinNumber, PinAttributes.SYS_PIN_ATTRIBUTE_VALUE)
        );
    }

    void setDirection(Pin.Direction direction) throws Exception {
        KernelInterfaceFileIO.writeWith(directionFileWriter, direction.value);
    }

    void setValue(Pin.Value value) throws Exception {
        KernelInterfaceFileIO.writeWith(valueFileWriter, value.value);
    }

    int readValue() throws IOException {
        String value = KernelInterfaceFileIO.readWith(valueRandomFileReader);
        if(value.isEmpty() || value.length() > 1) {
            throw new IllegalStateException("Pin value must be 0 or 1");
        }

        // Reposition reader at the beginning of the file
        valueRandomFileReader.seek(0L);

        return value.charAt(0) - 48;
    }

    @Override
    protected void finalize() throws Throwable {
        directionFileWriter.close();
        valueFileWriter.close();
        valueRandomFileReader.close();
    }
}
