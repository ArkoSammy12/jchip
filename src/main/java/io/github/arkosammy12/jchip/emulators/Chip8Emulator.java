package io.github.arkosammy12.jchip.emulators;

import io.github.arkosammy12.jchip.Main;
import io.github.arkosammy12.jchip.base.*;
import io.github.arkosammy12.jchip.hardware.*;
import io.github.arkosammy12.jchip.util.*;
import io.github.arkosammy12.jchip.video.Chip8Display;
import io.github.arkosammy12.jchip.base.Display;

import javax.sound.sampled.LineUnavailableException;
import java.awt.event.KeyAdapter;

public class Chip8Emulator implements Emulator {

    private static final int IPF_THROTTLE_THRESHOLD = 1000000;

    protected final Processor processor;
    private final Memory memory;
    protected final Display display;
    private final KeyState keyState;
    private final SoundSystem soundSystem;
    private final Chip8Variant chip8Variant;
    protected final EmulatorConfig config;
    protected final int targetInstructionsPerFrame;
    protected int currentInstructionsPerFrame;
    protected final boolean displayWaitEnabled;
    private boolean isTerminated = false;

    private int waitFrames = 0;

    public Chip8Emulator(EmulatorConfig emulatorConfig) throws Exception {
        try {
            this.config = emulatorConfig;
            this.chip8Variant = emulatorConfig.getConsoleVariant();
            this.displayWaitEnabled = emulatorConfig.doDisplayWait();
            this.targetInstructionsPerFrame = emulatorConfig.getInstructionsPerFrame();
            this.currentInstructionsPerFrame = targetInstructionsPerFrame;
            this.keyState = new KeyState(this.config.getKeyboardLayout());
            this.soundSystem = new Chip8SoundSystem(this.chip8Variant);
            this.display = this.createDisplay(config, keyState);
            this.memory = new Chip8Memory(this.config.getRom(), this.chip8Variant, this.display.getCharacterSpriteFont());
            this.processor = this.createProcessor();
        } catch (Exception e) {
            this.close();
            throw new RuntimeException(e);
        }
    }

    protected Processor createProcessor() {
        return new Chip8Processor(this);
    }

    @Override
    public Processor getProcessor() {
        return this.processor;
    }

    @Override
    public Memory getMemory() {
        return this.memory;
    }

    @Override
    public Chip8Display getDisplay() {
        return ((Chip8Display) this.display);
    }

    protected Display createDisplay(EmulatorConfig emulatorConfig, KeyAdapter keyAdapter) {
        return new Chip8Display(emulatorConfig, keyAdapter);
    }

    @Override
    public KeyState getKeyState() {
        return this.keyState;
    }

    @Override
    public SoundSystem getSoundSystem() {
        return this.soundSystem;
    }

    @Override
    public Chip8Variant getChip8Variant() {
        return this.chip8Variant;
    }

    @Override
    public EmulatorConfig getEmulatorConfig() {
        return this.config;
    }

    @Override
    public void terminate() {
        this.isTerminated = true;
    }

    @Override
    public boolean isTerminated() {
        return this.isTerminated;
    }

    @Override
    public void tick() throws InvalidInstructionException, LineUnavailableException {
        long startOfFrame = System.nanoTime();
        this.runInstructionLoop();
        this.getDisplay().flush(this.currentInstructionsPerFrame);
        this.getSoundSystem().pushSamples(this.getProcessor().getSoundTimer());
        long endOfFrame = System.nanoTime();
        long frameTime = endOfFrame - startOfFrame;
        if (this.targetInstructionsPerFrame >= IPF_THROTTLE_THRESHOLD) {
            long adjust = (frameTime - Main.FRAME_INTERVAL) / 100;
            this.currentInstructionsPerFrame = Math.clamp(this.currentInstructionsPerFrame - adjust, 1, this.targetInstructionsPerFrame);
        }
    }

    protected void runInstructionLoop() throws InvalidInstructionException, LineUnavailableException {
        this.processor.decrementTimers();
        if (this.waitFrames > 0) {
            this.waitFrames--;
            return;
        }
        for (int i = 0; i < this.currentInstructionsPerFrame; i++) {
            int flags = this.processor.cycle();
            if (this.waitForVBlank(flags)) {
                break;
            }
            if (this.processor.shouldTerminate()) {
                this.terminate();
                break;
            }
            if (this.getKeyState().shouldTerminate()) {
                this.terminate();
                break;
            }
        }
    }

    protected boolean waitForVBlank(int flags) {
        if (this.config.doDisplayWait()) {
            if ((flags & Chip8Processor.DRAW_EXECUTED) != 0) {
                return true;
            } else if ((flags & Chip8Processor.LONG_DRAW_EXECUTED) != 0) {
                this.waitFrames = 1;
                return true;
            }
        }
        return false;
    }

    @Override
    public void close() throws Exception {
        if (this.display != null) {
            this.display.close();
        }
        if (this.soundSystem != null) {
            this.soundSystem.close();
        }
        if (this.processor != null) {
            this.processor.close();
        }
    }

}
