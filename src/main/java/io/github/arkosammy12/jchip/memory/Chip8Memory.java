package io.github.arkosammy12.jchip.memory;

import io.github.arkosammy12.jchip.exceptions.EmulatorException;
import io.github.arkosammy12.jchip.util.Chip8Variant;
import io.github.arkosammy12.jchip.util.HexSpriteFont;

import java.util.Arrays;

public class Chip8Memory {

    private final int[] rom;
    private final int programStart;
    private final Chip8Variant chip8Variant;

    private final int[] bytes;
    private final int memoryBoundsMask;

    public Chip8Memory(int[] rom, Chip8Variant chip8Variant, int programStart, int memorySize) {
        this.rom = new int[rom.length];
        System.arraycopy(rom, 0, this.rom, 0, rom.length);
        this.chip8Variant = chip8Variant;
        this.programStart = programStart;
        this.memoryBoundsMask = memorySize - 1;
        try {
            this.bytes = new int[memorySize];
            chip8Variant.getSpriteFont().getSmallFont().ifPresent(smallFont -> {
                for (int i = 0; i < smallFont.length; i++) {
                    int[] slice = smallFont[i];
                    int sliceLength = slice.length;
                    int offset = HexSpriteFont.SMALL_FONT_BEGIN_OFFSET + (sliceLength * i);
                    for (int j = 0; j < sliceLength; j++) {
                        this.bytes[offset + j] = slice[j] & 0xFF;
                    }
                }
            });
            chip8Variant.getSpriteFont().getBigFont().ifPresent(bigFont -> {
                for (int i = 0; i < bigFont.length; i++) {
                    int[] slice = bigFont[i];
                    int sliceLength = slice.length;
                    int offset = HexSpriteFont.BIG_FONT_BEGIN_OFFSET + (sliceLength * i);
                    for (int j = 0; j < sliceLength; j++) {
                        this.bytes[offset + j] = slice[j] & 0xFF;
                    }
                }
            });
            for (int i = 0; i < rom.length; i++) {
                this.bytes[i + programStart] = rom[i] & 0xFF;
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            throw new EmulatorException("ROM size too big for CHIP-8 variant " + chip8Variant.getDisplayName() + "!");
        }
    }

    public int getMemoryBoundsMask() {
        return this.memoryBoundsMask;
    }

    public final int readByte(int address) {
        // Reading from memory beyond valid addressing range is undefined behavior. Chosen action is to overflow the offset
        return this.bytes[address & this.memoryBoundsMask];
    }

    public final void writeByte(int address, int value) {
        // Writing to memory beyond valid addressing range is undefined behavior. Chosen action is to overflow the offset
        this.bytes[address & this.memoryBoundsMask] = value;
    }

    public final int[] getMemoryView(int[] ret) {
        System.arraycopy(this.bytes, 0, ret, 0, Math.clamp(this.bytes.length, 0, ret.length));
        return ret;
    }

    public void reset() {
        Arrays.fill(this.bytes, 0);
        chip8Variant.getSpriteFont().getSmallFont().ifPresent(smallFont -> {
            for (int i = 0; i < smallFont.length; i++) {
                int[] slice = smallFont[i];
                int sliceLength = slice.length;
                int offset = HexSpriteFont.SMALL_FONT_BEGIN_OFFSET + (sliceLength * i);
                for (int j = 0; j < sliceLength; j++) {
                    this.bytes[offset + j] = slice[j] & 0xFF;
                }
            }
        });
        chip8Variant.getSpriteFont().getBigFont().ifPresent(bigFont -> {
            for (int i = 0; i < bigFont.length; i++) {
                int[] slice = bigFont[i];
                int sliceLength = slice.length;
                int offset = HexSpriteFont.BIG_FONT_BEGIN_OFFSET + (sliceLength * i);
                for (int j = 0; j < sliceLength; j++) {
                    this.bytes[offset + j] = slice[j] & 0xFF;
                }
            }
        });
        for (int i = 0; i < rom.length; i++) {
            this.bytes[i + programStart] = rom[i] & 0xFF;
        }
    }

}
