package io.github.arkosammy12.jchip.emulators;

import io.github.arkosammy12.jchip.config.settings.Chip8EmulatorSettings;
import io.github.arkosammy12.jchip.emulators.cpu.StrictChip8Processor;
import io.github.arkosammy12.jchip.emulators.bus.StrictChip8Bus;
import io.github.arkosammy12.jchip.ui.debugger.DebuggerSchema;
import io.github.arkosammy12.jchip.emulators.video.StrictChip8Display;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

import static io.github.arkosammy12.jchip.emulators.cpu.Chip8Processor.WAITING;
import static io.github.arkosammy12.jchip.emulators.cpu.Chip8Processor.isSet;

public final class StrictChip8Emulator extends Chip8Emulator {

    @Nullable
    private StrictChip8Processor processor;

    @Nullable
    private StrictChip8Bus bus;

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
    public StrictChip8Bus getBus() {
        return Objects.requireNonNull(this.bus);
    }

    @Override
    @NotNull
    protected StrictChip8Processor createProcessor() {
        this.processor = new StrictChip8Processor(this);
        return this.processor;
    }

    @Override
    @NotNull
    protected StrictChip8Display createDisplay() {
        this.display = new StrictChip8Display(this);
        return this.display;
    }

    @Override
    @NotNull
    protected StrictChip8Bus createBus() {
        this.bus = new StrictChip8Bus(this);
        return this.bus;
    }

    @Override
    public int getCurrentInstructionsPerFrame() {
        int ret = this.cycleCounter;
        this.cycleCounter = 0;
        return ret;
    }

    @Override
    public void executeFrame() {
        if (this.disassembler.isEnabled()) {
            this.runCyclesDebug();
        } else {
            this.runCycles();
        }
    }

    private void runCycles() {
        long nextFrame = this.nextFrame;
        while (this.machineCycles < nextFrame) {
            this.runCycle();
        }
    }

    private void runCyclesDebug() {
        long nextFrame = this.nextFrame;
        while (this.machineCycles < nextFrame) {
            this.disassembler.disassemble(this.getProcessor().getProgramCounter());
            if (this.disassembler.checkBreakpoint(this.getProcessor().getProgramCounter())) {
                this.jchip.onBreakpoint();
                break;
            }
            this.runCycle();
        }
    }

    private void runCycle() {
        if (!isSet(this.getProcessor().cycle(), WAITING)) {
            this.cycleCounter++;
        }
    }

    @Override
    public void executeCycle() {
        this.disassembler.disassembleRange(this.getProcessor().getProgramCounter(), 30, true);
        this.runCycle();
        this.disassembler.disassembleRange(this.getProcessor().getProgramCounter(), 30, false);
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
    protected DebuggerSchema createDebuggerSchema() {
        DebuggerSchema debuggerSchema = super.createDebuggerSchema();
        debuggerSchema.clearTextSectionEntries();
        debuggerSchema.setTextSectionName("Strict CHIP-8");
        debuggerSchema.createTextEntry()
                .withName("No custom quirks supported.");
        return debuggerSchema;
    }

}
