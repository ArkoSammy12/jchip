package io.github.arkosammy12.jchip.sound;

public interface SoundWriter {

    void pushSamples(byte[] samples);

    void volumeUp();

    void volumeDown();

}
