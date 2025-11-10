package io.github.arkosammy12.jchip.memory;

public class StrictChip8Memory extends Chip8Memory {

    public static final int STACK_OFFSET = 0xEA0;
    public static final int WORK_AREA_OFFSET = 0xED0;
    public static final int REGISTERS_OFFSET = 0xEF0;
    public static final int DISPLAY_OFFSET = 0xF00;

    public StrictChip8Memory(int[] rom) {
        super(rom);
    }

    public void setRegister(int index, int value) {
        this.writeByte(REGISTERS_OFFSET + index, value);
    }

    public int getRegister(int index) {
        return this.readByte(REGISTERS_OFFSET + index);
    }

    public void writeStackWord(int index, int value) {
        this.writeByte(this.getMemorySize() - 0x130 - index * 2 - 2, (value & 0xFF00) >>> 8);
        this.writeByte(this.getMemorySize() - 0x130 - index * 2 - 1, value & 0xFF);
    }

    public int readStackWord(int index) {
        return (this.readByte(this.getMemorySize() - 0x130 - index * 2 - 2) << 8) | this.readByte(this.getMemorySize() - 0x130 - index * 2 - 1);
    }

    @Override
    public void writeByte(int address, int value) {
        super.writeByte(address, value & 0xFF);
    }

    public void drawDisplayPixel(int column, int row) {
        int mask = 0x80 >>> (column & 7);
        int offset = (row << 3) + (column >>> 3);
        this.writeByte(DISPLAY_OFFSET + offset, this.readByte(DISPLAY_OFFSET + offset) ^ mask);
    }

    public boolean getDisplayPixel(int column, int row) {
        int mask = 0x80 >>> (column & 7);
        int offset = (row << 3) + (column >>> 3);
        return (this.readByte(DISPLAY_OFFSET + offset) & mask) != 0;
    }

}
