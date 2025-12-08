package io.github.arkosammy12.jchip.sound;

public interface SoundWriter {

    void pushSamples(byte[] samples);

    void setVolume(int volume);

    //void volumeUp();

    //void volumeDown();

}
