package io.github.arkosammy12.jchip.emulators;

import io.github.arkosammy12.jchip.Main;
import io.github.arkosammy12.jchip.base.*;
import io.github.arkosammy12.jchip.cpu.*;
import io.github.arkosammy12.jchip.memory.Chip8Memory;
import io.github.arkosammy12.jchip.sound.Chip8SoundSystem;
import io.github.arkosammy12.jchip.util.*;
import io.github.arkosammy12.jchip.video.Chip8Display;

import java.awt.event.KeyAdapter;

public class Chip8Emulator<D extends Chip8Display, S extends SoundSystem> implements Emulator {

    private static final int IPF_THROTTLE_THRESHOLD = 1000000;

    protected final Chip8VariantProcessor processor;
    private final Memory memory;
    protected final D display;
    private final Keypad keyState;
    protected final S soundSystem;
    private final Chip8Variant chip8Variant;
    protected final EmulatorConfig config;
    protected final int targetInstructionsPerFrame;
    protected int currentInstructionsPerFrame;
    protected final boolean displayWaitEnabled;
    private boolean isTerminated = false;

    private int waitFrames = 0;

    public Chip8Emulator(EmulatorConfig emulatorConfig) {
        try {
            this.config = emulatorConfig;
            this.chip8Variant = emulatorConfig.getConsoleVariant();
            this.displayWaitEnabled = emulatorConfig.doDisplayWait();
            this.targetInstructionsPerFrame = emulatorConfig.getInstructionsPerFrame();
            this.currentInstructionsPerFrame = targetInstructionsPerFrame;
            this.keyState = new Keypad(this.config.getKeyboardLayout());
            this.soundSystem = this.createSoundSystem(this.chip8Variant);
            this.display = this.createDisplay(config, keyState);
            this.memory = this.createMemory(this.config.getRom(), this.chip8Variant, this.display.getCharacterSpriteFont());
            this.processor = this.createProcessor();
        } catch (Exception e) {
            this.close();
            throw new RuntimeException(e);
        }
    }

    protected Chip8VariantProcessor createProcessor() {
        return new Chip8Processor<>(this);
    }

    @SuppressWarnings("unchecked")
    protected D createDisplay(EmulatorConfig emulatorConfig, KeyAdapter keyAdapter) {
        return (D) new Chip8Display(emulatorConfig, keyAdapter);
    }

    protected Chip8Memory createMemory(int[] program, Chip8Variant chip8Variant, SpriteFont spriteFont) {
        return new Chip8Memory(program, chip8Variant, spriteFont, 0x200, 0xFFF + 1);
    }

    @Override
    public Chip8VariantProcessor getProcessor() {
        return this.processor;
    }

    @Override
    public Memory getMemory() {
        return this.memory;
    }

    @Override
    public D getDisplay() {
        return this.display;
    }

    @Override
    public Keypad getKeyState() {
        return this.keyState;
    }

    @Override
    public S getSoundSystem() {
        return this.soundSystem;
    }

    @SuppressWarnings("unchecked")
    protected S createSoundSystem(Chip8Variant chip8Variant) {
        return (S) new Chip8SoundSystem(chip8Variant);
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
    public void tick() throws InvalidInstructionException {
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

    protected void runInstructionLoop() throws InvalidInstructionException {
        this.processor.decrementTimers();
        if (this.waitFrames > 0) {
            this.waitFrames--;
            return;
        }
        for (int i = 0; i < this.currentInstructionsPerFrame; i++) {
            int flags = this.processor.cycle();
            if (this.waitFrameEnd(flags)) {
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

    protected boolean waitFrameEnd(int flags) {
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
    public void close() {
        try {
            if (this.display != null) {
                this.display.close();
            }
        } catch (Exception e) {
            System.err.println("Error releasing emulator display resources: " + e);
        }
        try {
            if (this.soundSystem != null) {
                this.soundSystem.close();
            }
        } catch (Exception e) {
            System.err.println("Error releasing emulator sound system resources: " + e);
        }
    }

}
