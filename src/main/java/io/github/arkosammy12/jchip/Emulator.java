package io.github.arkosammy12.jchip;

import io.github.arkosammy12.jchip.io.*;
import io.github.arkosammy12.jchip.memory.Memory;
import io.github.arkosammy12.jchip.processor.DisplayInstruction;
import io.github.arkosammy12.jchip.processor.Instruction;
import io.github.arkosammy12.jchip.processor.Instructions;
import io.github.arkosammy12.jchip.processor.Processor;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class Emulator {

    private final Memory memory;
    private final EmulatorScreen emulatorScreen;
    private final Processor processor = new Processor();
    private final KeyState keyState = new KeyState();
    private final CharacterFont characterFont;
    private boolean terminated = false;
    private boolean displayInstructionExecuted = false;
    private final ProgramArgs programArgs;

    public Emulator(ProgramArgs programArgs) throws IOException {
        this.programArgs = programArgs;
        Path programPath = this.programArgs.getRomPath();
        if (!programPath.isAbsolute()) {
            programPath = programPath.toAbsolutePath();
        }
        byte[] programAsBytes = Files.readAllBytes(programPath);
        int[] program = new int[programAsBytes.length];
        for (int i = 0; i < program.length; i++) {
            program[i] = programAsBytes[i] & 0xFF;
        }
        KeyAdapter keyAdapter = new KeyAdapter() {

            @Override
            public void keyTyped(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                    terminated = true;
                }
                char c = e.getKeyChar();
                int keyCode = Utils.getIntegerForCharacter(c);
                if (keyCode < 0) {
                    return;
                }
                keyState.setKeyPressed(keyCode);
            }

            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                    terminated = true;
                }
                char c = e.getKeyChar();
                int keyCode = Utils.getIntegerForCharacter(c);
                if (keyCode < 0) {
                    return;
                }
                keyState.setKeyPressed(keyCode);
            }
            @Override
            public void keyReleased(KeyEvent e) {
                char c = e.getKeyChar();
                int keyCode = Utils.getIntegerForCharacter(c);
                if (keyCode < 0) {
                    return;
                }
                keyState.setKeyUnpressed(keyCode);
            }
        };
        this.emulatorScreen = new EmulatorScreen(this, keyAdapter);
        this.characterFont = new CharacterFont(this);
        this.memory = new Memory(program, this.characterFont);
    }

    public void tick(boolean decrementTimers) throws IOException {
        if (this.getProcessor().getSoundTimer() > 0) {
            this.getEmulatorScreen().beep();
        } else {
            this.getEmulatorScreen().stopBeep();
        }
        int[] newBytes = this.fetch();
        Instruction instruction = Instructions.decodeBytes(newBytes[0], newBytes[1]);
        this.processor.execute(this, instruction, decrementTimers);

        if (instruction instanceof DisplayInstruction) {
            this.displayInstructionExecuted = true;
        }
        this.getEmulatorScreen().flush();
    }

    private int[] fetch() {
        int programCounter = this.getProcessor().getProgramCounter();
        return new int[] {
                this.getMemory().read(programCounter),
                this.getMemory().read(programCounter + 1)
        };
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

    public boolean isTerminated() {
        return this.terminated;
    }

    public void close() throws IOException {
        this.emulatorScreen.close();
    }

    public boolean displayInstructionExecuted() {
        boolean returnValue = this.displayInstructionExecuted;
        if (returnValue) {
            this.displayInstructionExecuted = false;
        }
        return returnValue;
    }

    public ProgramArgs getProgramArgs() {
        return this.programArgs;
    }

    public ConsoleVariant getConsoleVariant() {
        return this.programArgs.getConsoleVariant();
    }

    public CharacterFont getCharacterFont() {
        return this.characterFont;
    }

}
