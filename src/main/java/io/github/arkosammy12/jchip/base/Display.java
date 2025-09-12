package io.github.arkosammy12.jchip.base;

import io.github.arkosammy12.jchip.util.CharacterSpriteFont;

import java.io.Closeable;
import java.io.IOException;

public interface Display extends Closeable {

    int getWidth();

    int getHeight();

    CharacterSpriteFont getCharacterFont();

    boolean togglePixel(int bitPlaneIndex, int column, int row);

    void setPixel(int bitPlaneIndex, int column, int row, boolean value);

    boolean getPixel(int bitPlaneIndex, int column, int row);

    void setExtendedMode(boolean extendedMode);

    boolean isExtendedMode();

    void setSelectedBitPlanes(int selectedBitPlanes);

    int getSelectedBitPlanes();

    void scrollUp(int scrollAmount);

    void scrollDown(int scrollAmount);

    void scrollRight();

    void scrollLeft();

    void clear();

    void flush(int currentInstructionsPerFrame) throws IOException;

}
