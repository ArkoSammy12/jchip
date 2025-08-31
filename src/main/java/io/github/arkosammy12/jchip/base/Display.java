package io.github.arkosammy12.jchip.base;

import io.github.arkosammy12.jchip.util.CharacterFont;

import java.io.Closeable;
import java.io.IOException;

public interface Display extends Closeable {

    int getWidth();

    int getHeight();

    CharacterFont getCharacterFont();

    boolean togglePixel(int bitPlane, int column, int row);

    void setPixel(int bitPlane, int column, int row, boolean value);

    boolean getPixel(int bitPlane, int column, int row);

    void setExtendedMode(boolean extendedMode);

    boolean isExtendedMode();

    void scrollUp(int scrollAmount, int bitPlane);

    void scrollDown(int scrollAmount, int bitPlane);

    void scrollRight(int bitPlane);

    void scrollLeft(int bitPlane);

    void clear(int bitPlane);

    void flush() throws IOException;

}
