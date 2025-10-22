package io.github.arkosammy12.jchip.emulators;

import io.github.arkosammy12.jchip.config.EmulatorConfig;
import io.github.arkosammy12.jchip.cpu.Chip8Processor;
import io.github.arkosammy12.jchip.cpu.StrictChip8Processor;
import io.github.arkosammy12.jchip.sound.Chip8SoundSystem;
import io.github.arkosammy12.jchip.video.Chip8Display;

public final class StrictChip8Emulator extends Chip8Emulator<Chip8Display, Chip8SoundSystem> {

    private long machineCycles;
    private long nextFrame;

    public StrictChip8Emulator(EmulatorConfig emulatorConfig) {
        super(emulatorConfig);
        // Amount of cycles the COSMAC-VIP needs to set things up before beginning execution of the ROM
        this.machineCycles = 3250;
        this.nextFrame = this.calculateNextFrame();
    }

    @Override
    public void tick() {
        long nextFrame = this.nextFrame;
        while (this.machineCycles < nextFrame) {
            this.getProcessor().cycle();
        }
        this.getSoundSystem().pushSamples(this.getProcessor().getSoundTimer());
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

    public long getCyclesLeftInCurrentFrame() {
        return this.nextFrame - this.machineCycles;
    }

    private void handleInterrupt() {
        this.getProcessor().decrementTimers();
        this.getDisplay().flush();
    }

    private long calculateNextFrame() {
        return ((this.machineCycles + 2572) / 3668) * 3668 + 1096;
    }

}
