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
    private final AudioSystem audioSystem;
    private final ConsoleVariant consoleVariant;
    protected final EmulatorConfig config;
    protected final int targetInstructionsPerFrame;
    protected int currentInstructionsPerFrame;
    protected final boolean displayWaitEnabled;
    private boolean isTerminated = false;

    public Chip8Emulator(EmulatorConfig emulatorConfig) throws IOException {
        this.config = emulatorConfig;
        this.consoleVariant = emulatorConfig.getConsoleVariant();
        this.displayWaitEnabled = emulatorConfig.doDisplayWait();
        ColorPalette colorPalette = emulatorConfig.getColorPalette();
        this.targetInstructionsPerFrame = emulatorConfig.getInstructionsPerFrame();
        int[] rom = this.config.getRom();
        this.currentInstructionsPerFrame = targetInstructionsPerFrame;
        this.keyState = new KeyState(this.config.getKeyboardLayout());
        this.audioSystem = new DefaultAudioSystem(this.consoleVariant);
        this.display = new BufferedImageDisplay(config.getProgramTitle(), this.consoleVariant, this.keyState, colorPalette);
        this.memory = new DefaultMemory(rom, this.consoleVariant, this.display.getCharacterFont());
        this.processor = this.createProcessor();
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
    public AudioSystem getAudioSystem() {
        return this.audioSystem;
    }

    @Override
    public ConsoleVariant getConsoleVariant() {
        return this.consoleVariant;
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
    public void tick(long startOfFrame) throws IOException, InvalidInstructionException {
        this.runInstructionLoop();
        this.getDisplay().flush(this.currentInstructionsPerFrame);
        this.getAudioSystem().pushSamples(this.getProcessor().getSoundTimer());
        long endOfFrame = System.nanoTime();
        long deltaTime = endOfFrame - startOfFrame;
        if (deltaTime != Main.FRAME_INTERVAL) {
            long adjust = (deltaTime - Main.FRAME_INTERVAL) / 10000;
            this.currentInstructionsPerFrame = Math.clamp(this.currentInstructionsPerFrame - adjust, 1, this.targetInstructionsPerFrame);
        }
    }

    protected void runInstructionLoop() throws InvalidInstructionException {
        for (int i = 0; i < this.currentInstructionsPerFrame; i++) {
            boolean shouldWaitForNextFrame = this.processor.cycle(i < 1);
            if (this.config.doDisplayWait() && shouldWaitForNextFrame) {
                break;
            }
            if (this.processor.shouldTerminate()) {
                this.terminate();
            }
            if (this.getKeyState().shouldTerminate()) {
                this.terminate();
            }
        }
    }

    @Override
    public void close() throws Exception {
        this.display.close();
        this.audioSystem.close();
    }

}
