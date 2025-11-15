package io.github.arkosammy12.jchip.emulators;

import io.github.arkosammy12.jchip.config.Chip8EmulatorSettings;
import io.github.arkosammy12.jchip.cpu.StrictChip8Processor;
import io.github.arkosammy12.jchip.memory.StrictChip8Memory;
import io.github.arkosammy12.jchip.video.StrictChip8Display;

import static io.github.arkosammy12.jchip.cpu.Chip8Processor.WAITING;
import static io.github.arkosammy12.jchip.cpu.Chip8Processor.isSet;

public final class StrictChip8Emulator extends Chip8Emulator {

    private StrictChip8Processor processor;
    private StrictChip8Memory memory;
    private StrictChip8Display display;

    private long machineCycles;
    private long nextFrame;
    private int cycleCounter;

    public StrictChip8Emulator(Chip8EmulatorSettings emulatorSettings) {
        super(emulatorSettings);
        // Amount of cycles the COSMAC-VIP needs to set things up before beginning execution of the ROM
        this.machineCycles = 3250;
        this.nextFrame = this.calculateNextFrame();
    }

    @Override
    public StrictChip8Processor getProcessor() {
        return this.processor;
    }

    @Override
    public StrictChip8Memory getMemory() {
        return this.memory;
    }

    @Override
    public StrictChip8Display getDisplay() {
        return this.display;
    }

    @Override
    public int getCurrentInstructionsPerFrame() {
        int ret = this.cycleCounter;
        this.cycleCounter = 0;
        return ret;
    }

    @Override
    public void executeFrame() {
        long nextFrame = this.nextFrame;
        while (this.machineCycles < nextFrame) {
            this.executeSingleCycle();
        }
    }

    @Override
    public void executeSingleCycle() {
        if (!isSet(this.getProcessor().cycle(), WAITING)) {
            this.cycleCounter++;
        }
    }

    protected void initializeDisplay() {
        this.display = new StrictChip8Display(this);
    }

    protected void initializeMemory() {
        this.memory = new StrictChip8Memory(this);
        this.memory.loadFont(this.getEmulatorSettings().getHexSpriteFont());
    }

    protected void initializeProcessor() {
        this.processor = new StrictChip8Processor(this);
    }

    public void addCycles(long cycles) {
        this.machineCycles += cycles;
        if (this.machineCycles >= this.nextFrame) {
            long irqTime = 1832 + (this.getProcessor().getSoundTimer() != 0 ? 4 : 0)  + (this.getProcessor().getDelayTimer() != 0 ? 8 : 0);
            this.handleInterrupt();
            this.machineCycles += irqTime;
            this.nextFrame = this.calculateNextFrame();
        }
    }

    public long getCyclesLeftInCurrentFrame() {
        return this.nextFrame - this.machineCycles;
    }

    private void handleInterrupt() {
        this.getSoundSystem().pushSamples(this.getProcessor().getSoundTimer());
        this.getProcessor().decrementTimers();
        this.getDisplay().flush();
    }

    private long calculateNextFrame() {
        return ((this.machineCycles + 2572) / 3668) * 3668 + 1096;
    }

}
