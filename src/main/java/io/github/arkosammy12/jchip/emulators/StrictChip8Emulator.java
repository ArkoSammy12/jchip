package io.github.arkosammy12.jchip.emulators;

import io.github.arkosammy12.jchip.config.EmulatorInitializer;
import io.github.arkosammy12.jchip.cpu.Chip8Processor;
import io.github.arkosammy12.jchip.cpu.StrictChip8Processor;
import io.github.arkosammy12.jchip.sound.Chip8SoundSystem;
import io.github.arkosammy12.jchip.video.Chip8Display;

import static io.github.arkosammy12.jchip.cpu.Chip8Processor.WAITING;
import static io.github.arkosammy12.jchip.cpu.Chip8Processor.isSet;

public final class StrictChip8Emulator extends Chip8Emulator<Chip8Display, Chip8SoundSystem> {

    private long machineCycles;
    private long nextFrame;
    private int cycleCounter;

    public StrictChip8Emulator(EmulatorInitializer emulatorInitializer) {
        super(emulatorInitializer, Chip8Display::new, Chip8SoundSystem::new);
        // Amount of cycles the COSMAC-VIP needs to set things up before beginning execution of the ROM
        this.machineCycles = 3250;
        this.nextFrame = this.calculateNextFrame();
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
            if (!isSet(this.getProcessor().cycle(), WAITING)) {
                this.cycleCounter++;
            }
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
