package smartbell.backend.model.kernelinterface;

import smartbell.backend.util.kernelinterface.KernelInterfaceFileIO;
import smartbell.backend.util.PinAttributes;

import java.io.*;

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

    private static class UdevPermissionTriger {

        private static final String UDEVADM_COMMAND = "udevadm";
        private static final String SETTLE_OPTION = "settle";
        private static final ProcessBuilder processBuilder = new ProcessBuilder(UDEVADM_COMMAND, SETTLE_OPTION);

        public static void updateKernelFilePermissionImmediately() throws IOException, InterruptedException {
            Process process = processBuilder.start();
            int exitCode = process.waitFor();

            if(exitCode != 0) {
                try(InputStreamReader ir = new InputStreamReader(process.getErrorStream());
                    BufferedReader err = new BufferedReader(ir)) {

                    // TODO log
                    err.lines().forEach(System.err::println);
                }
            }
        }

    }

     void export(String pinNumber) throws IOException, InterruptedException {
        if(!isExported(pinNumber)) {
            KernelInterfaceFileIO.writeWith(exportFileWriter, pinNumber);
            // This is required in order for the backend to run without superuser permission
            // The "udevadm settle" command is executed in order to guarantee that
            // the proper permissions are applied to the kernel interface files
            // before the backend attempts to write in them.
            UdevPermissionTriger.updateKernelFilePermissionImmediately();
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
