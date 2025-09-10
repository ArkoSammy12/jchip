package io.github.arkosammy12.jchip;


import io.github.arkosammy12.jchip.base.Emulator;
import io.github.arkosammy12.jchip.util.ConsoleVariant;
import io.github.arkosammy12.jchip.util.EmulatorConfig;

public class Main {

    public static final int FRAMES_PER_SECOND = 60;
    public static final long FRAME_INTERVAL = 1_000_000_000L / FRAMES_PER_SECOND;
    private static long lastSavedTime = System.nanoTime();

    public static void main(String[] args) {
        //EmulatorConfig emulatorConfig = CommandLine.populateSpec(EmulatorConfig.class, args);
        try (Emulator emulator = ConsoleVariant.getEmulatorForVariant(new EmulatorConfig(args))) {
            while (!emulator.isTerminated()) {
                long now = System.nanoTime();
                long deltaTime = now - lastSavedTime;
                if (deltaTime > FRAME_INTERVAL) {
                    emulator.tick(now);
                    lastSavedTime = now;
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}