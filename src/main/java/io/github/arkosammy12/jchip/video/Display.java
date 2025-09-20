package io.github.arkosammy12.jchip.video;

import io.github.arkosammy12.jchip.util.CharacterSpriteFont;

public interface Display {

    int getWidth();

    int getHeight();

    CharacterSpriteFont getCharacterSpriteFont();

    void clear();

    void flush(int currentInstructionsPerFrame);

    void close();

}
