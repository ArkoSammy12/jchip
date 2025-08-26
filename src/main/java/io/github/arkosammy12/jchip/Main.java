package io.github.arkosammy12.jchip;


import java.io.IOException;
import java.nio.file.Path;

public class Main {

    private static final int TICKS_PER_SECOND = 60;
    private static final long TICK_INTERVAL = 1_000_000_000L / TICKS_PER_SECOND;
    private static final int INSTRUCTIONS_PER_FRAME = 11;
    private static long lastSavedTime = System.nanoTime();

    public static void main(String[] args) throws IOException {
        String pathString = "";
        for (String arg : args) {
            if (!arg.startsWith("--")) {
                pathString = arg;
            }
        }
        Path path = Path.of(pathString);
        if (!path.isAbsolute()) {
            path = path.toAbsolutePath();
        }
        Emulator emulator = new Emulator(path);
        while (!emulator.isTerminated()) {
            long now = System.nanoTime();
            long deltaTime = now - lastSavedTime;
            if (deltaTime > TICK_INTERVAL) {
                for (int i = 0; i < INSTRUCTIONS_PER_FRAME; i++) {
                    emulator.tick(i < 1);
                    // Display wait quirk. COSMAC CHIP-8
                    if (emulator.displayInstructionExecuted()) {
                        break;
                    }
                }
                lastSavedTime = now;
            }
        }
        emulator.close();
    }

}