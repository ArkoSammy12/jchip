package io.github.arkosammy12.jchip.base;

import io.github.arkosammy12.jchip.util.SpriteFont;

import java.io.Closeable;

public interface Display extends Closeable {

    int getWidth();

    int getHeight();

    SpriteFont getCharacterSpriteFont();

    void clear();

    void flush(int currentInstructionsPerFrame);

}
