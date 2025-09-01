package io.github.arkosammy12.jchip.base;

public interface AudioSystem {

    void loadPatternByte(int index, int value);

    void setPitch(int pitch);

    void buzz();

    void stopBuzz();

}
