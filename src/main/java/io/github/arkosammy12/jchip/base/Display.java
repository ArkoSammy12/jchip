package io.github.arkosammy12.jchip.base;

import io.github.arkosammy12.jchip.util.CharacterFont;

import java.io.Closeable;
import java.io.IOException;

public interface Display extends Closeable {

    int getWidth();

    int getHeight();

    CharacterFont getCharacterFont();

    boolean togglePixel(int column, int row);

    void setExtendedMode(boolean extendedMode);

    boolean isExtendedMode();

    void scrollDown(int scrollAmount);

    void scrollRight();

    void scrollLeft();

    void clear();

    void flush() throws IOException;

}
