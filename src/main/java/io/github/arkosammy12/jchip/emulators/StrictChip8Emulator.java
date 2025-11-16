package io.github.arkosammy12.jchip.emulators;

import io.github.arkosammy12.jchip.config.Chip8EmulatorSettings;
import io.github.arkosammy12.jchip.cpu.StrictChip8Processor;
import io.github.arkosammy12.jchip.memory.StrictChip8Memory;
import io.github.arkosammy12.jchip.ui.debugger.DebuggerInfo;
import io.github.arkosammy12.jchip.video.StrictChip8Display;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

import static io.github.arkosammy12.jchip.cpu.Chip8Processor.WAITING;
import static io.github.arkosammy12.jchip.cpu.Chip8Processor.isSet;

public final class StrictChip8Emulator extends Chip8Emulator {

    @Nullable
    private StrictChip8Processor processor;

    @Nullable
    private StrictChip8Memory memory;

    @Nullable
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
    @NotNull
    public StrictChip8Processor getProcessor() {
        return Objects.requireNonNull(this.processor);
    }

    @Override
    @NotNull
    public StrictChip8Display getDisplay() {
        return Objects.requireNonNull(this.display);
    }

    @Override
    @NotNull
    public StrictChip8Memory getMemory() {
        return Objects.requireNonNull(this.memory);
    }

    @Override
    @Nullable
    protected StrictChip8Processor createProcessor() {
        this.processor = new StrictChip8Processor(this);
        return null;
    }

    @Override
    @Nullable
    protected StrictChip8Display createDisplay() {
        this.display = new StrictChip8Display(this);
        return null;
    }

    @Override
    @Nullable
    protected StrictChip8Memory createMemory() {
        this.memory = new StrictChip8Memory(this);
        return null;
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

    @Override
    protected DebuggerInfo createDebuggerInfo() {
        DebuggerInfo debuggerInfo = super.createDebuggerInfo();
        debuggerInfo.clearTextSectionEntries();
        debuggerInfo.setTextSectionName("Strict CHIP-8");
        debuggerInfo.createTextSectionEntry()
                .withName("No custom quirks supported.");
        return debuggerInfo;
    }

}
