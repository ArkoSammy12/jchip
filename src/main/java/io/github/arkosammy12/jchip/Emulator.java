package io.github.arkosammy12.jchip;

import io.github.arkosammy12.jchip.io.EmulatorScreen;
import io.github.arkosammy12.jchip.io.KeyState;
import io.github.arkosammy12.jchip.memory.Memory;
import io.github.arkosammy12.jchip.processor.Processor;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class Emulator {

    private final Memory memory;
    private final EmulatorScreen emulatorScreen;
    private final Processor processor = new Processor();
    private final KeyState keyState = new KeyState();

    public Emulator(Path programPath) throws IOException {
        byte[] programAsBytes = Files.readAllBytes(programPath);
        int[] program = new int[programAsBytes.length];
        for (int i = 0; i < program.length; i++) {
            program[i] = programAsBytes[i] & 0xFF;
        }
        this.memory = new Memory(program);
        /*
        KeyAdapter keyAdapter = new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (javax.swing.SwingUtilities.isEventDispatchThread()) {
                    System.out.println("Running on EDT (event handler)");
                } else {
                    System.out.println("Running on main emulator thread");
                }
                char c = e.getKeyChar();
                int keyCode = Utils.getIntegerForCharacter(c);
                if (keyCode < 0) {
                    return;
                }
                System.out.println("pressed" + c);
                keyState.setKeyPressed(keyCode);
            }
            @Override
            public void keyReleased(KeyEvent e) {
                if (javax.swing.SwingUtilities.isEventDispatchThread()) {
                    System.out.println("Running on EDT (event handler)");
                } else {
                    System.out.println("Running on main emulator thread");
                }
                char c = e.getKeyChar();
                int keyCode = Utils.getIntegerForCharacter(c);
                if (keyCode < 0) {
                    return;
                }
                System.out.println("released" + c);
                keyState.setKeyUnpressed(keyCode);
            }
        };

         */
        emulatorScreen = new EmulatorScreen();
    }

    public void tick(boolean decrementTimers) throws IOException {
        this.keyState.updateKeyState(this);
        this.processor.cycle(this, decrementTimers);
    }

    public int[] fetch() {
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
}
