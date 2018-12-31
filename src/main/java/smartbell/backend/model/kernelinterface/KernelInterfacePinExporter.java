package smartbell.backend.model.kernelinterface;

import smartbell.backend.util.kernelinterface.KernelInterfaceFileIO;
import smartbell.backend.util.PinAttributes;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

class KernelInterfacePinExporter {
    private final FileWriter exportFileWriter;
    private final FileWriter unexportFileWriter;

    KernelInterfacePinExporter() {
        exportFileWriter = KernelInterfaceFileIO.getFileWriterFor(KernelInterfaceFileIO.SYS_GPIO_EXPORT_FILE_PATH);
        unexportFileWriter = KernelInterfaceFileIO.getFileWriterFor(KernelInterfaceFileIO.SYS_GPIO_UNEXPORT_FILE_PATH);
    }

    private boolean isExported(String pinNumber) {
        return new File(KernelInterfaceFileIO
                .getPinAttributePath(pinNumber, PinAttributes.SYS_PIN_ATTRIBUTE_VALUE)
        ).exists();
    }

     void export(String pinNumber) throws IOException {
        if(!isExported(pinNumber)) {
            KernelInterfaceFileIO.writeWith(exportFileWriter, pinNumber);
        }
    }

    void unexport(String pinNumber) throws IOException {
        if(isExported(pinNumber)) {
            KernelInterfaceFileIO.writeWith(unexportFileWriter, pinNumber);
        }
    }

    @Override
    protected void finalize() throws Throwable {
        exportFileWriter.close();
        unexportFileWriter.close();
    }
}
