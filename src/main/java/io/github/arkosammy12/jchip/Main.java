package io.github.arkosammy12.jchip;


import java.io.IOException;
import java.nio.file.Path;

public class Main {

    private static final int TICKS_PER_SECOND = 720;
    private static final long SKIP_TICKS = 1_000_000_000L / TICKS_PER_SECOND;
    private static long nextTick = System.nanoTime();
    private static long tickCount = 12;

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
        while (true) {
            long now = System.nanoTime();
            while (now > nextTick) {
                boolean decrementTimers = false;
                if (tickCount <= 0) {
                    decrementTimers = true;
                    tickCount = 12;
                }
                if (emulator.getProcessor().getSoundTimer() > 0) {
                    emulator.getEmulatorScreen().beep();
                } else {
                    emulator.getEmulatorScreen().stopBeep();
                }
                emulator.tick(decrementTimers);
                nextTick += SKIP_TICKS;
                tickCount--;
            }

        }

    }

}