package smartbell.backend.model.kernelinterface;

import smartbell.backend.model.*;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class KernelInterfacePinManager implements PinManager {

    private final KernelInterfacePinExporter pinExporter;
    private final Set<Pin> exportedPins;

    public KernelInterfacePinManager() {
        pinExporter = new KernelInterfacePinExporter();
        exportedPins = new HashSet<>();
    }

    @Override
    public Pin exportPin(PinNumberSystem pinNumberSystem) throws Exception {
        String pinNumber = pinNumberSystem.getBCMPinNumber();
        pinExporter.export(pinNumber); // Set up sysfs if not set up already

        Pin pin = new KernelInterfacePin(pinNumber);
        if (exportedPins.contains(pin)) {
            throw new IllegalStateException("Pin with number: " + pinNumber + "/"
                    + pinNumberSystem.getWPiPinNumber() + " already exported!");
        }

        exportedPins.add(pin);
        return pin;
    }

    @Override
    public void unexportPin(Pin pin) throws Exception {
        pinExporter.unexport(pin.getNumber());
        exportedPins.remove(pin);
    }

    @Override
    public Pin provisionPin(PinNumberSystem pinNumberSystem, Pin.Direction direction) throws Exception {
        Pin pin = exportPin(pinNumberSystem);
        pin.setDirection(direction);

        return pin;
    }

    @Override
    public void resetPins() {
        try {
            for(Iterator<Pin> iterator = exportedPins.iterator(); iterator.hasNext();) {
                Pin currentPin = iterator.next();
                currentPin.removeOnValueChangedListener();
                pinExporter.unexport(currentPin.getNumber());
                iterator.remove();
            }
        } catch (Exception e) {
            e.printStackTrace();
            // TODO log
        }
    }

    private class KernelInterfacePin extends Pin {
        private static final long LISTENER_PROBE_PERIOD_MILLIS = 50L;

        private final KernelInterfaceAttributeManager attributeManager;
        private ScheduledExecutorService executor;

        KernelInterfacePin(String pinNumber) {
            super(pinNumber);
            attributeManager = new KernelInterfaceAttributeManager(this.pinNumber);
            executor = Executors.newSingleThreadScheduledExecutor();
        }

        @Override
        public void setDirection(Direction direction) throws Exception {
            attributeManager.setDirection(direction);
            cachedDirection = direction;
        }

        @Override
        public void setValue(Value value) throws Exception {
            attributeManager.setValue(value);
        }

        @Override
        public int getValue() throws Exception {
            return attributeManager.readValue();
        }

        @Override
        public void setOnValueChangedListener(OnValueChangedListener listener) {
            if(executor.isTerminated()) {
                executor = Executors.newSingleThreadScheduledExecutor();
            }

            Debouncer valueChangeDebouncer = new Debouncer(listener, debounceMillis, LISTENER_PROBE_PERIOD_MILLIS);
            executor.scheduleAtFixedRate(() -> {
                try {
                    valueChangeDebouncer.debounce(getValue());
                } catch (Exception e) {
                    // TODO log
                    e.printStackTrace();
                    executor.shutdown();
                }
            }, 0L, LISTENER_PROBE_PERIOD_MILLIS, TimeUnit.MILLISECONDS);
        }

        @Override
        public void removeOnValueChangedListener() {
            if(!executor.isShutdown()) {
                executor.shutdown();
            }
        }
    }
}
