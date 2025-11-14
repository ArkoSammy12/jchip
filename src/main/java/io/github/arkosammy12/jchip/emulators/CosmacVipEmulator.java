package io.github.arkosammy12.jchip.emulators;

import io.github.arkosammy12.jchip.config.EmulatorSettings;
import io.github.arkosammy12.jchip.cpu.CDP1802;
import io.github.arkosammy12.jchip.exceptions.EmulatorException;
import io.github.arkosammy12.jchip.memory.CosmacVipMemory;
import io.github.arkosammy12.jchip.sound.SoundSystem;
import io.github.arkosammy12.jchip.ui.IODevice;
import io.github.arkosammy12.jchip.util.Chip8Variant;
import io.github.arkosammy12.jchip.util.CosmacVIPKeypad;
import io.github.arkosammy12.jchip.video.CDP1861;

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
    private final CosmacVIPKeypad keypad;
    private final  IODevice[] ioDevices = new IODevice[8];

    private final boolean withChip8Interpreter;

    public CosmacVipEmulator(EmulatorSettings emulatorSettings, boolean withChip8Interpreter) {
        this.settings = emulatorSettings;
        this.withChip8Interpreter = withChip8Interpreter;
        this.chip8Variant = emulatorSettings.getVariant();
        this.keypad = new CosmacVIPKeypad(this.settings, this);
        this.processor = new CDP1802(this);
        this.memory = new CosmacVipMemory(this);
        this.display = new CDP1861<>(this);
        this.ioDevices[0] = this.display;
        this.ioDevices[1] = this.keypad;
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
        return List.of(this.keypad);
    }

    public int dispatchInput(int ioIndex) {
        IODevice ioDevice = this.ioDevices[(ioIndex - 1) & 0xF];
        if (ioDevice == null) {
            return 0;
        }
        return ioDevice.onInput();
    }

    public void dispatchOutput(int ioIndex, int value) {
        if (ioIndex >= 4) {
            this.memory.setRomShadowed(false);
            return;
        }
        IODevice ioDevice = this.ioDevices[(ioIndex - 1) & 0xF];
        if (ioDevice == null) {
            return;
        }
        ioDevice.onOutput(value);
    }

    public boolean isWithChip8Interpreter() {
        return this.withChip8Interpreter;
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
        this.display.doDmaOut(value);
    }

    public int dispatchDmaIn() {
        return this.display.doDmaIn();
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
            this.processor.cycle();
            this.display.cycle();
            this.keypad.cycle();
            this.processor.nextState();
        }
        this.display.flush();
    }

    @Override
    public void executeSingleCycle() {
        this.processor.cycle();
        this.display.cycle();
        this.processor.nextState();
        this.display.flush();
    }

    @Override
    public int getCurrentInstructionsPerFrame() {
        return CYCLES_PER_FRAME;
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
}
