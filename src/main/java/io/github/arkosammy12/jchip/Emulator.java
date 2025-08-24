package io.github.arkosammy12.jchip;

import com.googlecode.lanterna.input.KeyStroke;
import io.github.arkosammy12.jchip.display.EmulatorScreen;
import io.github.arkosammy12.jchip.memory.Memory;
import io.github.arkosammy12.jchip.processor.Processor;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.ConcurrentLinkedQueue;

public class Emulator {

    private final Memory memory;
    private final EmulatorScreen emulatorScreen = new EmulatorScreen();
    private final Processor processor = new Processor();
    private final ConcurrentLinkedQueue<KeyStroke> keyInputQueue = new ConcurrentLinkedQueue<>();

    public Emulator(Path programPath) throws IOException {

        byte[] programAsBytes = Files.readAllBytes(programPath);
        int[] program = new int[programAsBytes.length];

        for (int i = 0; i < program.length; i++) {
            program[i] = programAsBytes[i] & 0xFF;
        }

        this.memory = new Memory(program);

    }

    public void tick(boolean decrementTimers) throws IOException {
        this.processor.cycle(this, decrementTimers);
    }

    public int[] fetch(int programCounter) {
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

}
