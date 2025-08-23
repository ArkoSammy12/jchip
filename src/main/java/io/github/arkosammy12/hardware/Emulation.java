package io.github.arkosammy12.hardware;

import io.github.arkosammy12.display.EmulatorScreen;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class Emulation {

    private final Memory memory;
    private final EmulatorScreen emulatorScreen = new EmulatorScreen();
    private final Processor processor = new Processor();
    private int[] program;

    public Emulation(Path programPath) throws IOException {

        byte[] programAsBytes = Files.readAllBytes(programPath);
        this.program = new int[programAsBytes.length];

        for (int i = 0; i < program.length; i++) {
            program[i] = programAsBytes[i] & 0xFF;
        }

        this.memory = new Memory(this.program);

    }

    public void tick(boolean decrementTimers) throws IOException {
        this.processor.cycle(this);
    }

    public int[] fetch(int programCounter) {
        return new int[] {
                this.getMemory().read(programCounter + 0x200),
                this.getMemory().read(programCounter + 1 + 0x200)
        };
    }

    public Memory getMemory() {
        return this.memory;
    }

    public EmulatorScreen getEmulatorScreen() {
        return this.emulatorScreen;
    }

}
