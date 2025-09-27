package io.github.arkosammy12.jchip.base;

import java.io.Closeable;

public interface Display extends Closeable {

    int getWidth();

    int getHeight();

    void clear();

    void flush(int currentInstructionsPerFrame);

}
