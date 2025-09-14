package io.github.arkosammy12.jchip.base;

import java.io.Closeable;

public interface SoundSystem extends Closeable {

    void loadPatternByte(int index, int value);

    void setPlaybackRate(int pitch);

    void pushSamples(int soundTimer);

}
