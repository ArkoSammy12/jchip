package io.github.arkosammy12.jchip;


import io.github.arkosammy12.hardware.Emulation;

import java.io.IOException;
import java.nio.file.Path;

public class Main {

    private static final int TICKS_PER_SECOND = 720;
    private static final long SKIP_TICKS = 1_000_000_000L / TICKS_PER_SECOND;
    private static long nextTick = System.nanoTime();

    public static void main(String[] args) throws IOException {

        String path = "C:\\Users\\james\\Downloads\\ibm_logo.ch8";
        Emulation emulation = new Emulation(Path.of(path));

        while (true) {
            long now = System.nanoTime();

            while (now > nextTick) {
                emulation.tick(false);
                nextTick += SKIP_TICKS;
            }

        }



    }

}