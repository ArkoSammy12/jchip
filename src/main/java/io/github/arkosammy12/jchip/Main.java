package io.github.arkosammy12.jchip;

import io.github.arkosammy12.jchip.emulators.Chip8Emulator;
import io.github.arkosammy12.jchip.util.Chip8Variant;
import io.github.arkosammy12.jchip.config.EmulatorConfig;
import org.tinylog.Logger;

public class Main {

    public static final int FRAMES_PER_SECOND = 60;
    public static final long FRAME_INTERVAL = 1_000_000_000L / FRAMES_PER_SECOND;
    public static final String VERSION_STRING = "v2.2.0";

    static void main(String[] args) {
        try (Chip8Emulator<?, ?> emulator = Chip8Variant.getEmulator(new EmulatorConfig(args))) {
            long lastFrameTime = System.nanoTime();
            while (!emulator.isTerminated()) {
                long now = System.nanoTime();
                long elapsed = now - lastFrameTime;
                if (elapsed > 1_000_000_000L) {
                    lastFrameTime = now;
                    continue;
                }
                while (elapsed >= FRAME_INTERVAL) {
                    emulator.tick();
                    lastFrameTime += FRAME_INTERVAL;
                    elapsed -= FRAME_INTERVAL;
                }
            }
        } catch (Exception e) {
            Logger.error("jchip has crashed!");
            throw new RuntimeException(e);
        }
    }
}