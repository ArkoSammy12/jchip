package io.github.arkosammy12.jchip.emulators;

import io.github.arkosammy12.jchip.Main;
import io.github.arkosammy12.jchip.config.Chip8EmulatorSettings;
import io.github.arkosammy12.jchip.cpu.*;
import io.github.arkosammy12.jchip.exceptions.EmulatorException;
import io.github.arkosammy12.jchip.exceptions.InvalidInstructionException;
import io.github.arkosammy12.jchip.memory.Chip8Memory;
import io.github.arkosammy12.jchip.sound.Chip8SoundSystem;
import io.github.arkosammy12.jchip.util.*;
import io.github.arkosammy12.jchip.video.Chip8Display;

import java.awt.event.KeyAdapter;
import java.util.List;

import static io.github.arkosammy12.jchip.cpu.Chip8Processor.isSet;

public class Chip8Emulator implements Emulator {

    private static final int IPF_THROTTLE_THRESHOLD = 1000000;

    // Always access these via their getter methods
    private Chip8Processor<?> processor;
    private Chip8Memory memory;
    private Chip8Display<?> display;
    private Chip8SoundSystem soundSystem;

    private final Keypad keypad;
    private final Variant variant;
    private final Chip8EmulatorSettings emulatorSettings;
    private final int targetInstructionsPerFrame;

    private long instructionCounter;
    private int currentInstructionsPerFrame;
    private int waitFrames = 0;

    public Chip8Emulator(Chip8EmulatorSettings emulatorSettings) {
        try {
            this.emulatorSettings = emulatorSettings;
            this.variant = emulatorSettings.getVariant();
            this.targetInstructionsPerFrame = emulatorSettings.getInstructionsPerFrame();
            this.currentInstructionsPerFrame = targetInstructionsPerFrame;
            this.keypad = new Keypad(this);
            this.initializeComponents();
            this.getMemory().loadFont(emulatorSettings.getHexSpriteFont());
        } catch (Exception e) {
            this.close();
            throw new EmulatorException(e);
        }
    }

    protected void initializeComponents() {
        this.initializeSoundSystem();
        this.initializeDisplay();
        this.initializeMemory();
        this.initializeProcessor();
    }

    protected void initializeSoundSystem() {
        this.soundSystem = new Chip8SoundSystem(this);
    }

    protected void initializeDisplay() {
        this.display = new Chip8Display<>(this);
    }

    protected void initializeMemory() {
        this.memory = new Chip8Memory(this);
    }

    protected void initializeProcessor() {
        this.processor = new Chip8Processor<>(this);
    }

    @Override
    public Chip8Processor<?> getProcessor() {
        return this.processor;
    }

    @Override
    public Chip8Memory getMemory() {
        return this.memory;
    }

    @Override
    public Chip8Display<?> getDisplay() {
        return this.display;
    }

    @Override
    public Chip8SoundSystem getSoundSystem() {
        return this.soundSystem;
    }

    public Keypad getKeypad() {
        return this.keypad;
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
    public Chip8EmulatorSettings getEmulatorSettings() {
        return this.emulatorSettings;
    }

    public int getCurrentInstructionsPerFrame() {
        return this.currentInstructionsPerFrame;
    }

    @Override
    public void executeFrame() throws InvalidInstructionException {
        this.instructionCounter = 0;
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
        this.getProcessor().decrementTimers();
        if (this.waitFrames > 0) {
            this.waitFrames--;
            return;
        }
        for (int i = 0; i < this.currentInstructionsPerFrame; i++) {
            if (this.waitFrameEnd(this.getProcessor().cycle())) {
                break;
            }
            if (this.getProcessor().shouldTerminate()) {
                this.terminate();
                break;
            }
        }
    }

    @Override
    public void executeSingleCycle() {
        if (this.instructionCounter % this.currentInstructionsPerFrame == 0) {
            this.getProcessor().decrementTimers();
        }
        this.getProcessor().cycle();
        if (this.getProcessor().shouldTerminate()) {
            this.terminate();
        }
        this.getDisplay().flush();
        this.instructionCounter++;
    }

    protected boolean waitFrameEnd(int flags) {
        if (this.emulatorSettings.doDisplayWait()) {
            if (isSet(flags, Chip8Processor.DRAW_EXECUTED)) {
                return true;
            } else if (isSet(flags, Chip8Processor.LONG_INSTRUCTION)) {
                this.waitFrames = 1;
                return true;
            }
        }
        return false;
    }

    protected void terminate() {
        this.getEmulatorSettings().getJChip().stop();
    }

    @Override
    public void close() {
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
