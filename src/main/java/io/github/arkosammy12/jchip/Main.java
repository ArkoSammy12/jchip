package io.github.arkosammy12.jchip;


import io.github.arkosammy12.jchip.io.ProgramArgs;
import picocli.CommandLine;

import java.io.IOException;

public class Main {

    private static final long FRAME_INTERVAL = 1_000_000_000L / 60;
    private static long lastSavedTime = System.nanoTime();

    public static void main(String[] args) throws IOException {
        ProgramArgs programArgs = CommandLine.populateSpec(ProgramArgs.class, args);
        try (Emulator emulator = new Emulator(programArgs)) {
            while (!emulator.isTerminated()) {
                long now = System.nanoTime();
                long deltaTime = now - lastSavedTime;
                if (deltaTime > FRAME_INTERVAL) {
                    emulator.tick();
                    lastSavedTime = now;
                }
            }
        }
    }
}