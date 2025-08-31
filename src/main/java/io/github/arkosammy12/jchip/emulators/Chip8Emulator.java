package io.github.arkosammy12.jchip.emulators;

import io.github.arkosammy12.jchip.base.*;
import io.github.arkosammy12.jchip.hardware.DefaultAudioSystem;
import io.github.arkosammy12.jchip.hardware.DefaultMemory;
import io.github.arkosammy12.jchip.hardware.DefaultProcessor;
import io.github.arkosammy12.jchip.hardware.LanternaDisplay;
import io.github.arkosammy12.jchip.instructions.Draw;
import io.github.arkosammy12.jchip.instructions.ZeroOpcodeInstruction;
import io.github.arkosammy12.jchip.util.ConsoleVariant;
import io.github.arkosammy12.jchip.util.InvalidInstructionException;
import io.github.arkosammy12.jchip.util.KeyState;
import io.github.arkosammy12.jchip.util.ProgramArgs;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class Chip8Emulator implements Emulator {

    protected final Processor processor;
    private final Memory memory;
    private final Display display;
    private final KeyState keyState = new KeyState();
    private final AudioSystem audioSystem = new DefaultAudioSystem();
    private final ConsoleVariant consoleVariant;
    private final boolean debug;
    protected final int instructionsPerFrame;
    protected final boolean displayWaitEnabled;
    private boolean isTerminated = false;

    public Chip8Emulator(ProgramArgs programArgs) throws IOException {
        this.consoleVariant = programArgs.getConsoleVariant();
        this.debug = programArgs.debugEnabled();
        this.displayWaitEnabled = programArgs.isDisplayWaitEnabled();
        int instructionsPerFrame = programArgs.getInstructionsPerFrame();;
        if (instructionsPerFrame <= 0) {
            this.instructionsPerFrame = consoleVariant.getDefaultInstructionsPerFrame(programArgs.isDisplayWaitEnabled());
        } else {
            this.instructionsPerFrame = instructionsPerFrame;
        }
        Path romPath = programArgs.getRomPath();
        byte[] rawRom = Files.readAllBytes(romPath);
        int[] rom = new int[rawRom.length];
        for (int i = 0; i < rom.length; i++) {
            rom[i] = rawRom[i] & 0xFF;
        }
        this.display = new LanternaDisplay(this.consoleVariant, this.keyState);
        this.memory = new DefaultMemory(rom, this.consoleVariant, this.display.getCharacterFont());
        this.processor = new DefaultProcessor(this);
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
    public void terminate() {
        this.isTerminated = true;
    }

    @Override
    public boolean isTerminated() {
        return this.isTerminated;
    }

    @Override
    public void tick() throws IOException, InvalidInstructionException {
        for (int i = 0; i < this.instructionsPerFrame; i++) {
            Instruction executedInstruction = this.processor.cycle(i < 1);
            if (this.displayWaitEnabled && executedInstruction instanceof Draw) {
                break;
            }
            if (executedInstruction instanceof ZeroOpcodeInstruction zeroOpcodeInstruction && zeroOpcodeInstruction.shouldTerminateEmulator()) {
                this.terminate();
            }
            if (this.getKeyState().shouldTerminate()) {
                this.terminate();
            }
        }
        this.getDisplay().flush();
        if (this.getProcessor().getSoundTimer() > 0) {
            this.getAudioSystem().buzz();
        } else {
            this.getAudioSystem().stopBuzz();
        }
    }

    @Override
    public void close() throws Exception {
        this.display.close();
    }

}
