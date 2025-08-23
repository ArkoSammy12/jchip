package io.github.arkosammy12.jchip.memory;

import io.github.arkosammy12.jchip.display.CharacterSprites;

import java.util.List;

public class Memory {

    public static final int MEMORY_SIZE_BYTES = 4096;
    private final int[] bytes = new int[MEMORY_SIZE_BYTES];

    public Memory(int[] program) {

        List<CharacterSprites> charactersCharacters = CharacterSprites.getFontCharacters();

        for (CharacterSprites character : charactersCharacters) {

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

    public void store(int address, int value) {
        this.bytes[address] = value;
    }

}
