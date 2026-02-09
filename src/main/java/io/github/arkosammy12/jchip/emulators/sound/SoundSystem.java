package io.github.arkosammy12.jchip.emulators.sound;

public interface SoundSystem {

    int SAMPLE_RATE = 44100;

    void pushSamples(int soundTimer);

}
