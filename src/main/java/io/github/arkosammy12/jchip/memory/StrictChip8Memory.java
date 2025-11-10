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
        int stackWordOffset = this.getMemorySize() - 0x130 - index * 2;
        this.writeByte(stackWordOffset - 2, (value & 0xFF00) >>> 8);
        this.writeByte(stackWordOffset - 1, value & 0xFF);
    }

    public int readStackWord(int index) {
        int stackWordOffset = this.getMemorySize() - 0x130 - index * 2;
        return (this.readByte(stackWordOffset - 2) << 8) | this.readByte(stackWordOffset - 1);
    }

    public void drawDisplayPixel(int column, int row) {
        int mask = 0x80 >>> (column & 7);
        int offset = (row << 3) + (column >>> 3);
        int address = DISPLAY_OFFSET + offset;
        this.writeByte(address, this.readByte(address) ^ mask);
    }

    public boolean getDisplayPixel(int column, int row) {
        int mask = 0x80 >>> (column & 7);
        int offset = (row << 3) + (column >>> 3);
        return (this.readByte(DISPLAY_OFFSET + offset) & mask) != 0;
    }

    @Override
    public void writeByte(int address, int value) {
        if (address < 0 || address >= this.bytes.length) {
            return;
        }
        this.bytes[address] = value & 0xFF;
    }

    @Override
    public int readByte(int address) {
        if (address < 0 || address >= this.bytes.length) {
            // Bus pull up resistors ensure all lines are high
            return 0xFF;
        }
        return this.bytes[address];
    }

}
