package io.github.arkosammy12.jchip;


import io.github.arkosammy12.jchip.base.Emulator;
import io.github.arkosammy12.jchip.util.ConsoleVariant;
import io.github.arkosammy12.jchip.util.EmulatorConfig;

public class Main {

    public static final int FRAMES_PER_SECOND = 60;
    public static final long FRAME_INTERVAL = 1_000_000_000L / FRAMES_PER_SECOND;

    public static void main(String[] args) {
        try (Emulator emulator = ConsoleVariant.getEmulatorForVariant(new EmulatorConfig(args))) {
            long lastFrameTime = System.nanoTime();
            while (!emulator.isTerminated()) {
                long now = System.nanoTime();
                while (now - lastFrameTime >= FRAME_INTERVAL) {
                    lastFrameTime += FRAME_INTERVAL;
                    emulator.tick(lastFrameTime);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}