package io.github.arkosammy12.jchip;


import io.github.arkosammy12.jchip.io.ProgramArgs;
import picocli.CommandLine;

import java.io.IOException;

public class Main {

    private static final int TICKS_PER_SECOND = 60;
    private static final long TICK_INTERVAL = 1_000_000_000L / TICKS_PER_SECOND;
    private static final int INSTRUCTIONS_PER_FRAME = 11;
    private static long lastSavedTime = System.nanoTime();

    public static void main(String[] args) throws IOException {
        ProgramArgs programArgs = CommandLine.populateSpec(ProgramArgs.class, args);
        Emulator emulator = new Emulator(programArgs);
        while (!emulator.isTerminated()) {
            long now = System.nanoTime();
            long deltaTime = now - lastSavedTime;
            if (deltaTime > TICK_INTERVAL) {
                for (int i = 0; i < INSTRUCTIONS_PER_FRAME; i++) {
                    emulator.tick(i < 1);
                    if (emulator.shouldWaitForVBlank()) {
                        break;
                    }
                }
                lastSavedTime = now;
            }
        }
        emulator.close();
    }

}