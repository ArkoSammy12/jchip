package io.github.arkosammy12.jchip.sound;

public interface SoundWriter {

    void pushSamples8(byte[] samples);

    void pushSamples16(short[] samples);

    void setVolume(int volume);

}
