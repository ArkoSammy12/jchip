package io.github.arkosammy12.jchip.memory;

import io.github.arkosammy12.jchip.exceptions.EmulatorException;
import io.github.arkosammy12.jchip.util.HexSpriteFont;

public class Chip8Memory {

    private static final int MEMORY_BOUNDS_MASK = 0xFFF;
    private static final int MEMORY_SIZE = MEMORY_BOUNDS_MASK + 1;
    private static final int PROGRAM_START = 0x200;

    protected final int[] bytes;
    protected final int memoryBoundsMask;

    public Chip8Memory(int[] rom) {
        try {
            this.memoryBoundsMask = this.getMemoryBoundsMask();
            int programStart = this.getProgramStart();
            this.bytes = new int[this.getMemorySize()];
            for (int i = 0; i < rom.length; i++) {
                this.bytes[i + programStart] = rom[i] & 0xFF;
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            throw new EmulatorException("ROM size too big for selected CHIP-8 variant!");
        } catch (Exception e) {
            throw new EmulatorException("Error initializing CHIP-8 memory: ", e);
        }
    }

    public int getMemorySize() {
        return MEMORY_SIZE;
    }

    public int getMemoryBoundsMask() {
        return MEMORY_BOUNDS_MASK;
    }

    public int getProgramStart() {
        return PROGRAM_START;
    }

    public void loadFont(HexSpriteFont spriteFont) {
        try {
            spriteFont.getSmallFont().ifPresent(smallFont -> {
                for (int i = 0; i < smallFont.length; i++) {
                    int[] slice = smallFont[i];
                    int sliceLength = slice.length;
                    int offset = HexSpriteFont.SMALL_FONT_BEGIN_OFFSET + (sliceLength * i);
                    for (int j = 0; j < sliceLength; j++) {
                        this.bytes[offset + j] = slice[j] & 0xFF;
                    }
                }
            });
            spriteFont.getBigFont().ifPresent(bigFont -> {
                for (int i = 0; i < bigFont.length; i++) {
                    int[] slice = bigFont[i];
                    int sliceLength = slice.length;
                    int offset = HexSpriteFont.BIG_FONT_BEGIN_OFFSET + (sliceLength * i);
                    for (int j = 0; j < sliceLength; j++) {
                        this.bytes[offset + j] = slice[j] & 0xFF;
                    }
                }
            });
        } catch (Exception e) {
            throw new EmulatorException("Error initializing CHIP-8 font in memory: ", e);
        }
    }

    public int readByte(int address) {
        // Reading from memory beyond valid addressing range is undefined behavior. Chosen action is to overflow the offset
        return this.bytes[address & this.memoryBoundsMask];
    }

    public void writeByte(int address, int value) {
        // Writing to memory beyond valid addressing range is undefined behavior. Chosen action is to overflow the offset
        this.bytes[address & this.memoryBoundsMask] = value;
    }

}
