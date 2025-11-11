package io.github.arkosammy12.jchip.emulators;

import io.github.arkosammy12.jchip.Main;
import io.github.arkosammy12.jchip.config.EmulatorSettings;
import io.github.arkosammy12.jchip.cpu.*;
import io.github.arkosammy12.jchip.exceptions.EmulatorException;
import io.github.arkosammy12.jchip.exceptions.InvalidInstructionException;
import io.github.arkosammy12.jchip.memory.Chip8Memory;
import io.github.arkosammy12.jchip.sound.SoundSystem;
import io.github.arkosammy12.jchip.util.*;
import io.github.arkosammy12.jchip.video.Chip8Display;

import java.awt.event.KeyAdapter;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;

import static io.github.arkosammy12.jchip.cpu.Chip8Processor.isSet;

public class Chip8Emulator<M extends Chip8Memory, D extends Chip8Display, S extends SoundSystem> implements AutoCloseable {

    private static final int IPF_THROTTLE_THRESHOLD = 1000000;

    private final Chip8Processor<?, ?, ?, ?> processor;
    private final M memory;
    private final D display;
    private final S soundSystem;
    private final Keypad keypad;

    private final Chip8Variant chip8Variant;
    private final EmulatorSettings emulatorSettings;
    private final int targetInstructionsPerFrame;

    private long instructionCounter;
    private int currentInstructionsPerFrame;
    private int waitFrames = 0;

    public Chip8Emulator(
            EmulatorSettings emulatorSettings,
            Function<int[], M> memoryFactory,
            BiFunction<EmulatorSettings, List<KeyAdapter>, D> displayFactory,
            Function<EmulatorSettings, S> soundSystemFactory
    ) {
        try {
            this.emulatorSettings = emulatorSettings;
            this.chip8Variant = emulatorSettings.getVariant();
            this.targetInstructionsPerFrame = emulatorSettings.getInstructionsPerFrame();
            this.currentInstructionsPerFrame = targetInstructionsPerFrame;
            this.keypad = new Keypad(this.emulatorSettings);

            this.soundSystem = soundSystemFactory.apply(emulatorSettings);
            this.display = displayFactory.apply(this.emulatorSettings, List.of(this.keypad));

            this.memory = memoryFactory.apply(this.emulatorSettings.getRom());
            this.memory.loadFont(this.chip8Variant.getSpriteFont());

            this.processor = this.createProcessor();
        } catch (Exception e) {
            this.close();
            throw new EmulatorException(e);
        }
    }

    protected Chip8Processor<?, ?, ?, ?> createProcessor() {
        return new Chip8Processor<>(this);
    }

    public Chip8Processor<?, ?, ?, ?> getProcessor() {
        return this.processor;
    }

    public M getMemory() {
        return this.memory;
    }

    public D getDisplay() {
        return this.display;
    }

    public S getSoundSystem() {
        return this.soundSystem;
    }

    public Keypad getKeypad() {
        return this.keypad;
    }

    public Chip8Variant getChip8Variant() {
        return this.chip8Variant;
    }

    public EmulatorSettings getEmulatorInitializer() {
        return this.emulatorSettings;
    }

    public int getCurrentInstructionsPerFrame() {
        return this.currentInstructionsPerFrame;
    }

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

    public void executeSingleCycle() {
        if (this.instructionCounter % this.currentInstructionsPerFrame == 0) {
            this.processor.decrementTimers();
        }
        this.processor.cycle();
        if (this.processor.shouldTerminate()) {
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
        this.getEmulatorInitializer().getJChip().stop();
    }

    @Override
    public void close() {
        try {
            if (this.display != null) {
                this.display.close();
            }
            if (this.soundSystem != null) {
                this.soundSystem.close();
            }
        } catch (Exception e) {
            throw new EmulatorException("Error releasing current emulator resources: ", e);
        }
    }

}
