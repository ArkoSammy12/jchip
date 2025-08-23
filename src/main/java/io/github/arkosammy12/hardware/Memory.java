package io.github.arkosammy12.hardware;

import io.github.arkosammy12.utils.Font;

import java.util.List;

public class Memory {

    public static final int MEMORY_SIZE_BYTES = 4096;
    private final int[] bytes = new int[MEMORY_SIZE_BYTES];

    public Memory(int[] program) {

        List<Font> fontCharacters = Font.getFontCharacters();

        for (Font character : fontCharacters) {

            int[] bitmap = character.getBitmap();
            int offset = character.getOffset();
            for (int i = 0; i < bitmap.length; i++) {
                bytes[offset + i] = bitmap[i];
            }
        }

        for (int i = 0; i < program.length; i++) {
            bytes[i + 0x200] = program[i];
        }

    }

    public int read(int address) {
        return this.bytes[address];
    }

}
