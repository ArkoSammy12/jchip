package io.github.arkosammy12.jchip.emulators;

import io.github.arkosammy12.jchip.Main;
import io.github.arkosammy12.jchip.base.*;
import io.github.arkosammy12.jchip.hardware.*;
import io.github.arkosammy12.jchip.util.*;

import java.io.IOException;

public class Chip8Emulator implements Emulator {

    protected final Processor processor;
    private final Memory memory;
    private final Display display;
    private final KeyState keyState;
    private final SoundSystem soundSystem;
    private final Chip8Variant chip8Variant;
    protected final EmulatorConfig config;
    protected final int targetInstructionsPerFrame;
    protected int currentInstructionsPerFrame;
    protected final boolean displayWaitEnabled;
    private boolean isTerminated = false;

    public Chip8Emulator(EmulatorConfig emulatorConfig) throws Exception {
        try {
            this.config = emulatorConfig;
            this.chip8Variant = emulatorConfig.getConsoleVariant();
            this.displayWaitEnabled = emulatorConfig.doDisplayWait();
            this.targetInstructionsPerFrame = emulatorConfig.getInstructionsPerFrame();
            this.currentInstructionsPerFrame = targetInstructionsPerFrame;
            this.keyState = new KeyState(this.config.getKeyboardLayout());
            this.soundSystem = new Chip8SoundSystem(this.chip8Variant);
            this.display = new CanvasDisplay(config, this.keyState);
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
    public Display getDisplay() {
        return this.display;
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
    public void tick() throws IOException, InvalidInstructionException {
        long startOfFrame = System.nanoTime();
        this.runInstructionLoop();
        this.getDisplay().flush(this.currentInstructionsPerFrame);
        this.getSoundSystem().pushSamples(this.getProcessor().getSoundTimer());
        long endOfFrame = System.nanoTime();
        long frameTime = endOfFrame - startOfFrame;
        long adjust = (frameTime - Main.FRAME_INTERVAL) / 100;
        this.currentInstructionsPerFrame = Math.clamp(this.currentInstructionsPerFrame - adjust, 1, this.targetInstructionsPerFrame);
    }

    protected void runInstructionLoop() throws InvalidInstructionException {
        for (int i = 0; i < this.currentInstructionsPerFrame; i++) {
            int flags = this.processor.cycle(i < 1);
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
        return this.config.doDisplayWait() && (flags & Chip8Processor.DRAW_EXECUTED) != 0;
    }

    @Override
    public void close() throws Exception {
        if (this.display != null) {
            this.display.close();
        }
        if (this.soundSystem != null) {
            this.soundSystem.close();
        }
    }

}
