package io.github.arkosammy12.jchip.emulators;

import io.github.arkosammy12.jchip.JChip;
import io.github.arkosammy12.jchip.Main;
import io.github.arkosammy12.jchip.config.EmulatorConfig;
import io.github.arkosammy12.jchip.cpu.*;
import io.github.arkosammy12.jchip.exceptions.EmulatorException;
import io.github.arkosammy12.jchip.exceptions.InvalidInstructionException;
import io.github.arkosammy12.jchip.memory.Chip8Memory;
import io.github.arkosammy12.jchip.sound.Chip8SoundSystem;
import io.github.arkosammy12.jchip.sound.SoundSystem;
import io.github.arkosammy12.jchip.util.*;
import io.github.arkosammy12.jchip.video.Chip8Display;

import java.awt.event.KeyAdapter;
import java.util.List;

import static io.github.arkosammy12.jchip.cpu.Chip8Processor.isSet;

public class Chip8Emulator<D extends Chip8Display, S extends SoundSystem> implements AutoCloseable {

    private static final int IPF_THROTTLE_THRESHOLD = 1000000;

    private final Chip8Processor<?, ?, ?> processor;
    private final Chip8Memory memory;
    private final D display;
    private final S soundSystem;
    private final Keypad keypad;

    private final Chip8Variant chip8Variant;
    private final EmulatorConfig config;
    private final int targetInstructionsPerFrame;
    private int currentInstructionsPerFrame;
    private int waitFrames = 0;
    private boolean isTerminated = false;

    public Chip8Emulator(EmulatorConfig emulatorConfig) {
        try {
            this.config = emulatorConfig;
            this.chip8Variant = emulatorConfig.getVariant();
            this.targetInstructionsPerFrame = emulatorConfig.getInstructionsPerFrame();
            this.currentInstructionsPerFrame = targetInstructionsPerFrame;
            this.keypad = new Keypad(this.config.getKeyboardLayout());
            this.soundSystem = this.createSoundSystem(emulatorConfig.getJChip(), this.chip8Variant);
            this.display = this.createDisplay(config, List.of(this.keypad));
            this.memory = this.createMemory(this.config.getRom(), this.chip8Variant);
            this.processor = this.createProcessor();
        } catch (Exception e) {
            this.close();
            throw new EmulatorException(e);
        }
    }


    protected Chip8Processor<?, ?, ?> createProcessor() {
        return new Chip8Processor<>(this);
    }

    public Chip8Processor<?, ?, ?> getProcessor() {
        return this.processor;
    }

    protected Chip8Memory createMemory(int[] rom, Chip8Variant chip8Variant) {
        return new Chip8Memory(rom, chip8Variant, 0x200, 0xFFF + 1);
    }

    public Chip8Memory getMemory() {
        return this.memory;
    }

    @SuppressWarnings("unchecked")
    protected D createDisplay(EmulatorConfig emulatorConfig, List<KeyAdapter> keyAdapters) {
        return (D) new Chip8Display(emulatorConfig, keyAdapters);
    }

    public D getDisplay() {
        return this.display;
    }

    public S getSoundSystem() {
        return this.soundSystem;
    }

    @SuppressWarnings("unchecked")
    protected S createSoundSystem(JChip jchip, Chip8Variant chip8Variant) {
        return (S) new Chip8SoundSystem(jchip, chip8Variant);
    }

    public Keypad getKeypad() {
        return this.keypad;
    }

    public Chip8Variant getChip8Variant() {
        return this.chip8Variant;
    }

    public EmulatorConfig getEmulatorConfig() {
        return this.config;
    }

    public int getCurrentInstructionsPerFrame() {
        return this.currentInstructionsPerFrame;
    }

    protected void terminate() {
        this.isTerminated = true;
    }

    public boolean isTerminated() {
        return this.isTerminated;
    }

    public void tick() throws InvalidInstructionException {
        long startOfFrame = System.nanoTime();
        this.runInstructionLoop();
        this.getDisplay().flush();
        this.getSoundSystem().pushSamples(this.getProcessor().getSoundTimer());
        long endOfFrame = System.nanoTime();
        long frameTime = endOfFrame - startOfFrame;
        if (this.targetInstructionsPerFrame >= IPF_THROTTLE_THRESHOLD) {
            long adjust = (frameTime - Main.FRAME_INTERVAL) / 100;
            this.currentInstructionsPerFrame = Math.clamp(this.currentInstructionsPerFrame - adjust, 1, this.targetInstructionsPerFrame);
        }
    }

    protected void runInstructionLoop() throws InvalidInstructionException {
        this.processor.decrementTimers();
        if (this.waitFrames > 0) {
            this.waitFrames--;
            return;
        }
        for (int i = 0; i < this.currentInstructionsPerFrame; i++) {
            if (this.waitFrameEnd(this.processor.cycle())) {
                break;
            }
            if (this.processor.shouldTerminate()) {
                this.terminate();
                break;
            }
        }
    }

    protected boolean waitFrameEnd(int flags) {
        if (this.config.doDisplayWait()) {
            if (isSet(flags, Chip8Processor.DRAW_EXECUTED)) {
                return true;
            } else if (isSet(flags, Chip8Processor.LONG_DRAW_EXECUTED)) {
                this.waitFrames = 1;
                return true;
            }
        }
        return false;
    }

    public void close() {
        try {
            if (this.display != null) {
                this.display.close();
            }
            if (this.soundSystem != null) {
                this.soundSystem.close();
            }
        } catch (Exception e) {
            System.err.println("Error releasing emulator resources: " + e);
        }
    }

}
