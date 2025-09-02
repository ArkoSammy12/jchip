package io.github.arkosammy12.jchip.instructions;

import io.github.arkosammy12.jchip.base.ExecutionContext;
import io.github.arkosammy12.jchip.base.Instruction;

public abstract class AbstractInstruction implements Instruction {

    protected final ExecutionContext executionContext;

    private final int firstNibble;
    private final int secondNibble;
    private final int thirdNibble;
    private final int fourthNibble;
    private final int secondByte;
    private final int memoryAddress;

    public AbstractInstruction(int firstByte, int secondByte, ExecutionContext executionContext) {
        this.executionContext = executionContext;
        this.firstNibble = (firstByte  & 0xF0) >> 4;
        this.secondNibble = (firstByte  & 0x0F);
        this.thirdNibble = (secondByte & 0xF0) >> 4;
        this.fourthNibble = (secondByte & 0x0F);
        this.secondByte = secondByte;
        this.memoryAddress = ((firstByte << 8) | secondByte) & 0x0FFF;
    }

    @Override
    public int getSecondNibble() {
        return this.secondNibble;
    }

    @Override
    public int getThirdNibble() {
        return this.thirdNibble;
    }

    @Override
    public int getFourthNibble() {
        return this.fourthNibble;
    }

    @Override
    public int getSecondByte() {
        return this.secondByte;
    }

    @Override
    public int getMemoryAddress() {
        return this.memoryAddress;
    }

    @Override
    public String toString() {
        return "Instruction: %s%s%s%s".formatted(
                Integer.toHexString(this.firstNibble).toUpperCase(),
                Integer.toHexString(this.secondNibble).toUpperCase(),
                Integer.toHexString(this.thirdNibble).toUpperCase(),
                Integer.toHexString(this.fourthNibble).toUpperCase()
        );
    }

}
