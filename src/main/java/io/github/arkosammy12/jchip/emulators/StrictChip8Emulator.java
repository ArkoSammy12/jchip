package io.github.arkosammy12.jchip.emulators;

import io.github.arkosammy12.jchip.config.EmulatorConfig;
import io.github.arkosammy12.jchip.cpu.Chip8Processor;
import io.github.arkosammy12.jchip.cpu.StrictChip8Processor;
import io.github.arkosammy12.jchip.sound.Chip8SoundSystem;
import io.github.arkosammy12.jchip.video.Chip8Display;

public final class StrictChip8Emulator extends Chip8Emulator<Chip8Display, Chip8SoundSystem> {

    private long machineCycles;
    private long instructionCycles;
    private long nextFrame = 1122;
    private boolean waiting;

    public StrictChip8Emulator(EmulatorConfig emulatorConfig) {
        super(emulatorConfig);
    }

    @Override
    public void tick() {
        long nextFrame = this.nextFrame;
        while (this.machineCycles < nextFrame) {
            this.getProcessor().cycle();
        }
    }

    @Override
    protected Chip8Processor<?, ?, ?> createProcessor() {
        return new StrictChip8Processor(this);
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

    public long getInstructionCycles() {
        return this.instructionCycles;
    }

    public void setInstructionCycles(long instructionCycles) {
        this.instructionCycles = instructionCycles;
    }

    public void setWaiting(boolean waiting) {
        this.waiting = waiting;
    }

    public boolean isWaiting() {
        return this.waiting;
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
