package io.github.arkosammy12.jchip.base;

import java.io.Closeable;

public interface AudioSystem extends Closeable {

    void loadPatternByte(int index, int value);

    void setPlaybackRate(int pitch);

    void pushFrame(int soundTimer);

}
