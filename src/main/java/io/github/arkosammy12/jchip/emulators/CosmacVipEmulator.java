package io.github.arkosammy12.jchip.emulators;

import io.github.arkosammy12.jchip.config.CosmacVipEmulatorSettings;
import io.github.arkosammy12.jchip.config.EmulatorSettings;
import io.github.arkosammy12.jchip.cpu.CDP1802;
import io.github.arkosammy12.jchip.exceptions.EmulatorException;
import io.github.arkosammy12.jchip.memory.CosmacVipMemory;
import io.github.arkosammy12.jchip.sound.Chip8SoundSystem;
import io.github.arkosammy12.jchip.sound.SoundSystem;
import io.github.arkosammy12.jchip.ui.debugger.DebuggerInfo;
import io.github.arkosammy12.jchip.util.vip.IODevice;
import io.github.arkosammy12.jchip.util.Variant;
import io.github.arkosammy12.jchip.util.vip.CosmacVIPKeypad;
import io.github.arkosammy12.jchip.video.CDP1861;

import java.awt.event.KeyAdapter;
import java.util.List;
import java.util.function.Function;

import static io.github.arkosammy12.jchip.util.vip.IODevice.DmaStatus.IN;
import static io.github.arkosammy12.jchip.util.vip.IODevice.DmaStatus.OUT;

public class CosmacVipEmulator implements Emulator {

    public static final int CYCLES_PER_FRAME = 3668;

    private final CosmacVipEmulatorSettings settings;
    private final DebuggerInfo debuggerInfo;
    private final Variant variant;

    private final CDP1802 processor;
    private final CosmacVipMemory memory;
    private final CDP1861<?> display;
    private final Chip8SoundSystem soundSystem;
    private final CosmacVIPKeypad keypad;
    private final IODevice[] ioDevices = new IODevice[8];

    private final boolean isHybridChip8;
    private int currentInstructionsPerFrame;

    public CosmacVipEmulator(CosmacVipEmulatorSettings emulatorSettings, boolean isHybridChip8) {
        this.settings = emulatorSettings;
        this.isHybridChip8 = isHybridChip8;
        this.variant = emulatorSettings.getVariant();
        this.keypad = new CosmacVIPKeypad(this);
        this.processor = new CDP1802(this);
        this.memory = new CosmacVipMemory(this);
        this.display = new CDP1861<>(this);
        this.soundSystem = new Chip8SoundSystem(this);
        this.ioDevices[0] = this.display;
        this.ioDevices[1] = this.keypad;
        this.debuggerInfo = this.createDebuggerInfo();
    }

    @Override
    public CDP1802 getProcessor() {
        return this.processor;
    }

    @Override
    public CosmacVipMemory getMemory() {
        return this.memory;
    }

    @Override
    public CDP1861<?> getDisplay() {
        return this.display;
    }

    @Override
    public SoundSystem getSoundSystem() {
        return this.soundSystem;
    }

    @Override
    public EmulatorSettings getEmulatorSettings() {
        return this.settings;
    }

    @Override
    public Variant getChip8Variant() {
        return this.variant;
    }

    @Override
    public List<KeyAdapter> getKeyAdapters() {
        return List.of(this.keypad);
    }

    @Override
    public DebuggerInfo getDebuggerInfo() {
        return this.debuggerInfo;
    }

    public boolean isHybridChip8() {
        return this.isHybridChip8;
    }

    public int dispatchInput(int ioPort) {
        IODevice ioDevice = this.ioDevices[(ioPort - 1) & 0xF];
        if (ioDevice == null) {
            return 0;
        }
        return ioDevice.onInput();
    }

    public void dispatchOutput(int ioPort, int value) {
        if (ioPort >= 4) {
            this.memory.setMA7Latched(false);
            return;
        }
        IODevice ioDevice = this.ioDevices[(ioPort - 1) & 0xF];
        if (ioDevice == null) {
            return;
        }
        ioDevice.onOutput(value);
    }

    public IODevice.DmaStatus getDmaStatus() {
        IODevice.DmaStatus highestStatus = IODevice.DmaStatus.NONE;
        for (IODevice ioDevice : this.ioDevices) {
            if (ioDevice == null) {
                continue;
            }
            switch (ioDevice.getDmaStatus()) {
                case IN -> highestStatus = IN;
                case OUT -> {
                    if (highestStatus == IODevice.DmaStatus.NONE) {
                        highestStatus = OUT;
                    }
                }
            }
        }
        return highestStatus;
    }

    public void dispatchDmaOut(int value) {
        for (IODevice ioDevice : this.ioDevices) {
            if (ioDevice == null) {
                continue;
            }
            if (ioDevice.getDmaStatus() == IODevice.DmaStatus.OUT) {
                ioDevice.doDmaOut(value);
                return;
            }
        }
    }

    public int dispatchDmaIn() {
        for (IODevice ioDevice : this.ioDevices) {
            if (ioDevice == null) {
                continue;
            }
            if (ioDevice.getDmaStatus() == IODevice.DmaStatus.IN) {
                return ioDevice.doDmaIn();
            }
        }
        return 0xFF;
    }

    public boolean anyInterrupting() {
        for (IODevice ioDevice : this.ioDevices) {
            if (ioDevice == null) {
                continue;
            }
            if (ioDevice.isInterrupting()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void executeFrame() {
        for (int i = 0; i < CYCLES_PER_FRAME; i++) {

            CDP1802.State currentState = this.processor.getCurrentState();

            this.processor.cycle();
            this.tickIoDevices();
            this.processor.nextState();

            CDP1802.State nextState = this.processor.getCurrentState();

            if (currentState.isS1Execute() && !nextState.isS1Execute()) {
                this.currentInstructionsPerFrame++;
            }

        }
        this.display.flush();
        this.soundSystem.pushSamples(this.processor.getQ() ? 1 : 0);
    }

    @Override
    public void executeSingleCycle() {
        this.processor.cycle();
        this.tickIoDevices();
        this.processor.nextState();
        this.display.flush();
    }

    private void tickIoDevices() {
        for (IODevice ioDevice : this.ioDevices) {
            if (ioDevice != null) {
                ioDevice.cycle();
            }
        }
    }

    @Override
    public int getCurrentInstructionsPerFrame() {
        int ret = this.currentInstructionsPerFrame;
        this.currentInstructionsPerFrame = 0;
        return ret;
    }

    @Override
    public void close() throws Exception {
        try {
            if (this.getDisplay() != null) {
                this.getDisplay().close();
            }
            if (this.getSoundSystem() != null) {
                this.getSoundSystem().close();
            }
        } catch (Exception e) {
            throw new EmulatorException("Error releasing current emulator resources: ", e);
        }
    }

    protected DebuggerInfo createDebuggerInfo() {
        DebuggerInfo debuggerInfo = new DebuggerInfo();
        debuggerInfo.setTextSectionName("Cosmac VIP");
        debuggerInfo.setScrollAddressSupplier(() -> this.processor.getRegister(this.processor.getX()));

        Function<Integer, String> byteFormatter = val -> String.format("%02X", val);
        Function<Integer, String> nibbleFormatter = val -> String.format("%01X", val);
        Function<Boolean, String> booleanFormatter = val -> val ? "1" : "0";

        debuggerInfo.createTextSectionEntry()
                .withName("Cosmac VIP based variant.");
        debuggerInfo.createTextSectionEntry()
                .withName("Does not support custom quirks.");

        debuggerInfo.<Integer>createSingleRegisterSectionEntry()
                .withName("I")
                .withStateUpdater(this.processor::getI)
                .withToStringFunction(nibbleFormatter);

        debuggerInfo.<Integer>createSingleRegisterSectionEntry()
                .withName("N")
                .withStateUpdater(this.processor::getN)
                .withToStringFunction(nibbleFormatter);

        debuggerInfo.<Integer>createSingleRegisterSectionEntry()
                .withName("P")
                .withStateUpdater(this.processor::getP)
                .withToStringFunction(nibbleFormatter);

        debuggerInfo.<Integer>createSingleRegisterSectionEntry()
                .withName("X")
                .withStateUpdater(this.processor::getX)
                .withToStringFunction(nibbleFormatter);

        debuggerInfo.<Integer>createSingleRegisterSectionEntry()
                .withName("D")
                .withStateUpdater(this.processor::getD)
                .withToStringFunction(byteFormatter);

        debuggerInfo.<Integer>createSingleRegisterSectionEntry()
                .withName("T")
                .withStateUpdater(this.processor::getT)
                .withToStringFunction(byteFormatter);

        debuggerInfo.<Boolean>createSingleRegisterSectionEntry()
                .withName("DF")
                .withStateUpdater(this.processor::getDF)
                .withToStringFunction(booleanFormatter);

        debuggerInfo.<Boolean>createSingleRegisterSectionEntry()
                .withName("IE")
                .withStateUpdater(this.processor::getInterruptEnable)
                .withToStringFunction(booleanFormatter);

        debuggerInfo.<Boolean>createSingleRegisterSectionEntry()
                .withName("Q")
                .withStateUpdater(this.processor::getQ)
                .withToStringFunction(booleanFormatter);

        debuggerInfo.setStackSectionName("X Data");

        for (int i = 0; i < 16; i++) {
            int finalI = i;
            debuggerInfo.<Integer>createRegisterSectionEntry()
                    .withName(String.format("R%01X", i))
                    .withStateUpdater(() -> this.getProcessor().getRegister(finalI))
                    .withToStringFunction(val -> String.format("%04X", val));

            int finalI1 = i;
            debuggerInfo.<Integer>createStackSectionEntry()
                    .withName(String.format("%01X", i))
                    .withStateUpdater(() -> this.getMemory().getByte(this.processor.getRegister(finalI1)))
                    .withToStringFunction(val -> String.format("%02X", val));

        }
        return debuggerInfo;
    }

}
