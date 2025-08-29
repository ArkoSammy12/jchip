package io.github.arkosammy12.jchip;

import io.github.arkosammy12.jchip.io.*;
import io.github.arkosammy12.jchip.memory.Memory;
import io.github.arkosammy12.jchip.processor.DrawInstruction;
import io.github.arkosammy12.jchip.processor.Instruction;
import io.github.arkosammy12.jchip.processor.Instructions;
import io.github.arkosammy12.jchip.processor.Processor;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class Emulator implements AutoCloseable {

    private final Processor processor = new Processor();
    private final Memory memory;
    private final EmulatorScreen emulatorScreen;
    private final KeyState keyState = new KeyState();
    private final CharacterFont characterFont;
    private final ProgramArgs programArgs;
    private final ConsoleVariant consoleVariant;
    private final int instructionsPerFrame;
    private boolean terminated = false;

    public Emulator(ProgramArgs programArgs) throws IOException {
        this.programArgs = programArgs;
        this.consoleVariant = programArgs.getConsoleVariant();
        int instructionsPerFrame = programArgs.getInstructionsPerFrame();
        if (instructionsPerFrame <= 0) {
            this.instructionsPerFrame = consoleVariant.getDefaultInstructionsPerFrame(programArgs.isDisplayWaitEnabled());
        } else {
            this.instructionsPerFrame = instructionsPerFrame;
        }
        Path programPath = this.programArgs.getRomPath();
        if (!programPath.isAbsolute()) {
            programPath = programPath.toAbsolutePath();
        }
        byte[] programAsBytes = Files.readAllBytes(programPath);
        int[] program = new int[programAsBytes.length];
        for (int i = 0; i < program.length; i++) {
            program[i] = programAsBytes[i] & 0xFF;
        }
        this.emulatorScreen = new EmulatorScreen(this, this.keyState);
        this.characterFont = new CharacterFont(this);
        this.memory = new Memory(program, this.characterFont);
    }

    public Processor getProcessor() {
        return this.processor;
    }

    public Memory getMemory() {
        return this.memory;
    }

    public EmulatorScreen getEmulatorScreen() {
        return this.emulatorScreen;
    }

    public KeyState getKeyState() {
        return this.keyState;
    }

    public CharacterFont getCharacterFont() {
        return this.characterFont;
    }

    public ProgramArgs getProgramArgs() {
        return this.programArgs;
    }

    public ConsoleVariant getConsoleVariant() {
        return this.consoleVariant;
    }

    public void terminate() {
        this.terminated = true;
    }

    public boolean isTerminated() {
        return this.terminated;
    }

    public void tick() throws IOException {
        for (int i = 0; i < this.instructionsPerFrame; i++) {
            int[] newBytes = this.fetch();
            Instruction instruction = Instructions.decodeBytes(newBytes[0], newBytes[1]);
            this.processor.execute(this, instruction, i < 1);
            ConsoleVariant consoleVariant = this.getConsoleVariant();
            if (programArgs.isDisplayWaitEnabled() && instruction instanceof DrawInstruction && (consoleVariant == ConsoleVariant.CHIP_8 || (consoleVariant == ConsoleVariant.SUPER_CHIP_LEGACY && !this.getEmulatorScreen().isExtendedMode()))) {
                break;
            }
            if (this.getKeyState().shouldTerminate()) {
                this.terminated = true;
            }
        }
        this.getEmulatorScreen().flush();
        if (this.getProcessor().getSoundTimer() > 0) {
            this.getEmulatorScreen().buzz();
        } else {
            this.getEmulatorScreen().stopBuzz();
        }
    }

    private int[] fetch() {
        int programCounter = this.getProcessor().getProgramCounter();
        return new int[] {
                this.getMemory().read(programCounter),
                this.getMemory().read(programCounter + 1)
        };
    }

    public void close() throws IOException {
        this.emulatorScreen.close();
    }

}
