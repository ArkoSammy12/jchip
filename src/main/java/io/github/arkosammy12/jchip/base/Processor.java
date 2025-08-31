package io.github.arkosammy12.jchip.base;

import io.github.arkosammy12.jchip.util.InvalidInstructionException;

import java.util.Random;

public interface Processor {

    void setProgramCounter(int programCounter);

    void incrementProgramCounter();

    void decrementProgramCounter();

    int getProgramCounter();

    void setIndexRegister(int indexRegister);

    int getIndexRegister();

    void push(int value);

    int pop();

    void setDelayTimer(int value);

    int getDelayTimer();

    void setSoundTimer(int value);

    int getSoundTimer();

    void setRegister(int register, int value);

    void setBitPlane(int bitPlane);

    int getBitPlane();

    default void setCarry(boolean value) {
        this.setRegister(0xF, value ? 1 : 0);
    }

    int getRegister(int register);

    void saveFlags(int length);

    void loadFlags(int length);

    Random getRandom();

    Instruction cycle(boolean sixtiethOfASecond) throws InvalidInstructionException;

    static boolean nextOpcodeIsF000(Processor processor, Memory memory) {
        int currentProgramCounter = processor.getProgramCounter();
        int firstByte = memory.readByte(currentProgramCounter);
        int secondByte = memory.readByte(currentProgramCounter + 1);
        int opcode = (firstByte << 8) | secondByte;
        return opcode == 0xF000;
    }

}
