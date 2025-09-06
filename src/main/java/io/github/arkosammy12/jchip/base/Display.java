package io.github.arkosammy12.jchip.base;

import io.github.arkosammy12.jchip.util.CharacterFont;

import java.io.Closeable;
import java.io.IOException;

public interface Display extends Closeable {

    int getWidth();

    int getHeight();

    CharacterFont getCharacterFont();

    boolean togglePixel(int bitPlaneIndex, int column, int row);

    void setPixel(int bitPlaneIndex, int column, int row, boolean value);

    boolean getPixel(int bitPlaneIndex, int column, int row);

    void setExtendedMode(boolean extendedMode);

    boolean isExtendedMode();

    void scrollUp(int scrollAmount, int selectedBitPlanes);

    void scrollDown(int scrollAmount, int selectedBitPlanes);

    void scrollRight(int selectedBitPlanes);

    void scrollLeft(int selectedBitPlanes);

    void clear(int selectedBitPlanes);

    void flush(int currentInstructionsPerFrame) throws IOException;

}
