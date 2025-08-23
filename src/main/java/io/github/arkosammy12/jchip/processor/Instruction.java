package io.github.arkosammy12.jchip.processor;

import io.github.arkosammy12.jchip.Emulator;

import java.io.IOException;

public abstract class Instruction {

    private final int opcode;
    private final int secondNibble;
    private final int thirdNibble;
    private final int fourthNibble;
    private final int secondByte;
    private final int memoryAddress;

    public Instruction(int firstByte, int secondByte) {
        this.opcode = (firstByte  & 0xF0) >> 4;
        this.secondNibble = (firstByte  & 0x0F);
        this.thirdNibble = (secondByte & 0xF0) >> 4;
        this.fourthNibble = (secondByte & 0x0F);
        this.secondByte = secondByte;
        this.memoryAddress = ((firstByte << 8) | secondByte) & 0x0FFF;
    }

    public int getOpcode() {
        return this.opcode;
    }

    public int getSecondNibble() {
        return this.secondNibble;
    }

    public int getThirdNibble() {
        return this.thirdNibble;
    }

    public int getFourthNibble() {
        return this.fourthNibble;
    }

    public int getSecondByte() {
        return this.secondByte;
    }

    public int getMemoryAddress() {
        return this.memoryAddress;
    }

    abstract public void execute(Emulator emulator) throws IOException;

}
