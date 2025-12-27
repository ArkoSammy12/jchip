package io.github.arkosammy12.jchip.emulators;

import io.github.arkosammy12.jchip.Main;
import io.github.arkosammy12.jchip.config.Chip8EmulatorSettings;
import io.github.arkosammy12.jchip.cpu.*;
import io.github.arkosammy12.jchip.exceptions.EmulatorException;
import io.github.arkosammy12.jchip.exceptions.InvalidInstructionException;
import io.github.arkosammy12.jchip.memory.Chip8Bus;
import io.github.arkosammy12.jchip.sound.Chip8SoundSystem;
import io.github.arkosammy12.jchip.sound.SoundSystem;
import io.github.arkosammy12.jchip.ui.debugger.DebuggerSchema;
import io.github.arkosammy12.jchip.util.*;
import io.github.arkosammy12.jchip.video.Chip8Display;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.event.KeyAdapter;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

import static io.github.arkosammy12.jchip.cpu.Chip8Processor.isSet;

public class Chip8Emulator implements Emulator {

    private static final int IPF_THROTTLE_THRESHOLD = 1000000;

    @Nullable
    private final Chip8Processor<?> processor;

    @Nullable
    private final Chip8Bus bus;

    @Nullable
    private final Chip8Display<?> display;

    @Nullable
    private final SoundSystem soundSystem;

    private final Keypad keypad;
    private final Variant variant;
    private final Chip8EmulatorSettings emulatorSettings;
    private final DebuggerSchema debuggerSchema;
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

            this.soundSystem = this.createSoundSystem();
            this.display = this.createDisplay();
            this.bus = this.createBus();
            this.processor = this.createProcessor();

            this.getBus().loadFont(emulatorSettings.getHexSpriteFont());
            this.debuggerSchema = this.createDebuggerSchema();
        } catch (Exception e) {
            this.close();
            throw new EmulatorException(e);
        }
    }

    @Override
    @NotNull
    public Chip8Processor<?> getProcessor() {
        return Objects.requireNonNull(this.processor);
    }

    @Override
    @NotNull
    public Chip8Display<?> getDisplay() {
        return Objects.requireNonNull(this.display);
    }

    @Override
    @NotNull
    public Chip8Bus getBus() {
        return Objects.requireNonNull(this.bus);
    }

    @Override
    @NotNull
    public SoundSystem getSoundSystem() {
        return Objects.requireNonNull(this.soundSystem);
    }

    @Nullable
    protected Chip8Processor<?> createProcessor() {
        return new Chip8Processor<>(this);
    }

    @Nullable
    protected Chip8Display<?> createDisplay() {
        return new Chip8Display<>(this);
    }

    @Nullable
    protected Chip8Bus createBus() {
        return new Chip8Bus(this);
    }

    @Nullable
    protected SoundSystem createSoundSystem() {
        return new Chip8SoundSystem(this);
    }

    public Keypad getKeypad() {
        return this.keypad;
    }

    @Override
    public Variant getVariant() {
        return this.variant;
    }

    @Override
    public List<KeyAdapter> getKeyAdapters() {
        return List.of(this.keypad);
    }

    @Override
    public DebuggerSchema getDebuggerSchema() {
        return this.debuggerSchema;
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
            if (this.waitVBlank(this.getProcessor().cycle())) {
                break;
            }
            if (this.getProcessor().shouldExit()) {
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
        if (this.getProcessor().shouldExit()) {
            this.terminate();
        }
        this.getDisplay().flush();
        this.instructionCounter++;
    }

    protected boolean waitVBlank(int flags) {
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
        this.getEmulatorSettings().getJchip().stop();
    }

    @Override
    public void close() {
        try {
            this.getDisplay().close();
        } catch (Exception e) {
            throw new EmulatorException("Error releasing current emulator resources: ", e);
        }
    }

    protected DebuggerSchema createDebuggerSchema() {
        DebuggerSchema debuggerSchema = new DebuggerSchema();
        debuggerSchema.setScrollAddressSupplier(() -> this.getProcessor().getIndexRegister());

        debuggerSchema.createTextEntry()
                .withName("VF Reset: " + this.emulatorSettings.doVFReset() + ".");
        debuggerSchema.createTextEntry()
                .withName("I increment: " + this.emulatorSettings.getMemoryIncrementQuirk().getDisplayName() + ".");
        debuggerSchema.createTextEntry()
                .withName("Display Wait: " + this.emulatorSettings.doDisplayWait() + ".");
        debuggerSchema.createTextEntry()
                .withName("Clipping: " + this.emulatorSettings.doDisplayWait() + ".");
        debuggerSchema.createTextEntry()
                .withName("Shift VX In Place: " + this.emulatorSettings.doShiftVXInPlace() + ".");
        debuggerSchema.createTextEntry()
                .withName("Jump With VX: " + this.emulatorSettings.doJumpWithVX() + ".");

        Function<Integer, String> byteFormatter = val -> String.format("%02X", val);

        debuggerSchema.<Integer>createCpuRegisterEntry()
                .withName("PC")
                .withStateUpdater(this.getProcessor()::getProgramCounter)
                .withToStringFunction(val -> String.format("%0" + hexDigitCount(this.getBus().getMemoryBoundsMask()) + "X", val));

        debuggerSchema.<Integer>createCpuRegisterEntry()
                .withName("I")
                .withStateUpdater(this.getProcessor()::getIndexRegister)
                .withToStringFunction(val -> String.format("%0" + hexDigitCount(this.getBus().getMemoryBoundsMask()) + "X", val));

        debuggerSchema.<Integer>createCpuRegisterEntry()
                .withName("DT")
                .withStateUpdater(this.getProcessor()::getSoundTimer)
                .withToStringFunction(byteFormatter);

        debuggerSchema.<Integer>createCpuRegisterEntry()
                .withName("ST")
                .withStateUpdater(this.getProcessor()::getSoundTimer)
                .withToStringFunction(byteFormatter);

        debuggerSchema.<Integer>createCpuRegisterEntry()
                .withName("SP")
                .withStateUpdater(this.getProcessor()::getStackPointer)
                .withToStringFunction(byteFormatter);

        for (int i = 0; i < 16; i++) {
            int finalI = i;
            debuggerSchema.<Integer>createGeneralPurposeRegisterEntry()
                    .withName(String.format("V%01X", i))
                    .withStateUpdater(() -> this.getProcessor().getRegister(finalI))
                    .withToStringFunction(byteFormatter);

            debuggerSchema.<Integer>createStackEntry()
                    .withName(String.format("%01X", i))
                    .withStateUpdater(() -> this.getProcessor().getStackElement(finalI))
                    .withToStringFunction(val -> String.format("%0" + hexDigitCount(this.getBus().getMemoryBoundsMask()) + "X", val));

        }
        return debuggerSchema;
    }

    private static int hexDigitCount(int x) {
        if (x == 0) {
            return 1;
        }
        return (32 - Integer.numberOfLeadingZeros(x) + 3) >>> 2;
    }

}
