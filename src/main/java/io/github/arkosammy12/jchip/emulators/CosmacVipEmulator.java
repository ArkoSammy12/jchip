package io.github.arkosammy12.jchip.emulators;

import io.github.arkosammy12.jchip.config.EmulatorSettings;
import io.github.arkosammy12.jchip.cpu.CDP1802;
import io.github.arkosammy12.jchip.cpu.Processor;
import io.github.arkosammy12.jchip.memory.CosmacVipMemory;
import io.github.arkosammy12.jchip.memory.Memory;
import io.github.arkosammy12.jchip.sound.SoundSystem;
import io.github.arkosammy12.jchip.ui.IODevice;
import io.github.arkosammy12.jchip.util.Chip8Variant;
import io.github.arkosammy12.jchip.video.CDP1861;
import io.github.arkosammy12.jchip.video.Display;

import java.awt.event.KeyAdapter;
import java.util.List;

import static io.github.arkosammy12.jchip.ui.IODevice.DmaStatus.IN;
import static io.github.arkosammy12.jchip.ui.IODevice.DmaStatus.OUT;

public class CosmacVipEmulator implements Emulator {

    public static final int CYCLES_PER_FRAME = 3668;

    private final EmulatorSettings settings;
    private final Chip8Variant chip8Variant;

    private final CDP1802 processor;
    private final CosmacVipMemory memory;
    private final CDP1861<?> display;
    private final List<IODevice> ioDevices;

    private final boolean withChip8Interpreter;

    private long machineCycleCount;

    public CosmacVipEmulator(EmulatorSettings emulatorSettings, boolean withChip8Interpreter) {
        this.settings = emulatorSettings;
        this.withChip8Interpreter = withChip8Interpreter;
        this.chip8Variant = emulatorSettings.getVariant();

        this.processor = new CDP1802(this);
        this.memory = new CosmacVipMemory(this);
        this.display = new CDP1861<>(this);

        this.ioDevices = List.of(this.display);
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
        return null;
    }

    @Override
    public EmulatorSettings getEmulatorSettings() {
        return this.settings;
    }

    @Override
    public Chip8Variant getChip8Variant() {
        return this.chip8Variant;
    }

    @Override
    public List<KeyAdapter> getKeyAdapters() {
        return List.of();
    }

    public long getRelativeMachineCycleCount() {
        return this.machineCycleCount % CYCLES_PER_FRAME;
    }

    public long getAbsoluteMachineCycleCount() {
        return this.machineCycleCount;
    }

    public IODevice getIODevice(int ioCommandNumber) {
        int index = ioCommandNumber - 1;
        if (index < 0 || index >= this.ioDevices.size()) {
            return null;
        }
        return this.ioDevices.get(index);
    }

    public boolean isWithChip8Interpreter() {
        return this.withChip8Interpreter;
    }

    public IODevice.DmaStatus getHighestDmaStatus() {
        IODevice.DmaStatus highestStatus = IODevice.DmaStatus.NONE;
        for (IODevice ioDevice : this.ioDevices) {
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
        this.display.doDmaOut(value);
    }

    public int dispatchDmaIn() {
        return this.display.doDmaIn();
    }

    public boolean anyInterrupting() {
        for (IODevice ioDevice : this.ioDevices) {
            if (ioDevice.isInterrupting()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void executeFrame() {
        for (int i = 0; i < CYCLES_PER_FRAME; i++) {
            this.processor.cycle();
            this.display.cycle();
            this.processor.nextState();
        }
        this.display.flush();
    }

    @Override
    public void executeSingleCycle() {

    }

    @Override
    public int getCurrentInstructionsPerFrame() {
        return 0;
    }

    @Override
    public void close() throws Exception {

    }
}
