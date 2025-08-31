package io.github.arkosammy12.jchip.base;

import io.github.arkosammy12.jchip.util.CharacterFont;

import java.io.Closeable;
import java.io.IOException;

public interface Display extends Closeable {

    int getWidth();

    int getHeight();

    CharacterFont getCharacterFont();

    boolean togglePixel(int column, int row, int bitPlane);

    void setPixel(int column, int row, int bitPlane, boolean value);

    boolean getPixel(int column, int row, int bitPlane);

    void setExtendedMode(boolean extendedMode);

    boolean isExtendedMode();

    void scrollUp(int scrollAmount, int bitPlane);

    void scrollDown(int scrollAmount, int bitPlane);

    void scrollRight(int bitPlane);

    void scrollLeft(int bitPlane);

    void clear(int bitPlane);

    void flush() throws IOException;

}
