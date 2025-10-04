package io.github.arkosammy12.jchip.emulators;

import io.github.arkosammy12.jchip.Main;
import io.github.arkosammy12.jchip.cpu.*;
import io.github.arkosammy12.jchip.memory.Chip8Memory;
import io.github.arkosammy12.jchip.sound.Chip8SoundSystem;
import io.github.arkosammy12.jchip.sound.SoundSystem;
import io.github.arkosammy12.jchip.util.*;
import io.github.arkosammy12.jchip.video.Chip8Display;

import java.awt.event.KeyAdapter;

import static io.github.arkosammy12.jchip.cpu.Chip8Processor.isSet;

public class Chip8Emulator<D extends Chip8Display, S extends SoundSystem> implements AutoCloseable {

    private static final int IPF_THROTTLE_THRESHOLD = 1000000;

    protected final Chip8Processor<?, ?, ?> processor;
    private final Chip8Memory memory;
    protected final D display;
    protected final S soundSystem;
    private final Keypad keyState;
    private final Chip8Variant chip8Variant;
    protected final EmulatorConfig config;

    private final int targetInstructionsPerFrame;
    private int currentInstructionsPerFrame;
    private boolean isTerminated = false;
    private int waitFrames = 0;

    public Chip8Emulator(EmulatorConfig emulatorConfig) {
        try {
            this.config = emulatorConfig;
            this.chip8Variant = emulatorConfig.getConsoleVariant();
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

    protected Chip8Processor<?, ?, ?> createProcessor() {
        return new Chip8Processor<>(this);
    }

    public Chip8Processor<?, ?, ?> getProcessor() {
        return this.processor;
    }

    protected Chip8Memory createMemory(int[] program, Chip8Variant chip8Variant, SpriteFont spriteFont) {
        return new Chip8Memory(program, chip8Variant, spriteFont, 0x200, 0xFFF + 1);
    }

    public Chip8Memory getMemory() {
        return this.memory;
    }

    @SuppressWarnings("unchecked")
    protected D createDisplay(EmulatorConfig emulatorConfig, KeyAdapter keyAdapter) {
        return (D) new Chip8Display(emulatorConfig, keyAdapter);
    }

    public D getDisplay() {
        return this.display;
    }

    public S getSoundSystem() {
        return this.soundSystem;
    }

    @SuppressWarnings("unchecked")
    protected S createSoundSystem(Chip8Variant chip8Variant) {
        return (S) new Chip8SoundSystem(chip8Variant);
    }

    public Keypad getKeyState() {
        return this.keyState;
    }

    public Chip8Variant getChip8Variant() {
        return this.chip8Variant;
    }

    public EmulatorConfig getEmulatorConfig() {
        return this.config;
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
        this.getDisplay().flush(this.currentInstructionsPerFrame);
        this.getSoundSystem().pushSamples(this.getProcessor().getSoundTimer());
        long endOfFrame = System.nanoTime();
        long frameTime = endOfFrame - startOfFrame;
        //double frameTimeMs = frameTime / 1_000_000.0;
        //Logger.info(String.format("Frametime: %.3f ms", frameTimeMs));
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
