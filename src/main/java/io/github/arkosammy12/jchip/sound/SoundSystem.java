package io.github.arkosammy12.jchip.sound;

import io.github.arkosammy12.jchip.Main;

import java.io.Closeable;

public interface SoundSystem extends Closeable {

    int SAMPLE_RATE = 44100;
    int SAMPLES_PER_FRAME = SAMPLE_RATE / Main.FRAMES_PER_SECOND;

    void pushSamples(int soundTimer);

}
