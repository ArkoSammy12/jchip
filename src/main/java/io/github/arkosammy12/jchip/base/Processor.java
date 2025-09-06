package io.github.arkosammy12.jchip.base;

import io.github.arkosammy12.jchip.util.InvalidInstructionException;

public interface Processor {

    int getProgramCounter();

    int getDelayTimer();

    int getSoundTimer();

    int getSelectedBitPlanes();

    boolean cycle(boolean sixtiethOfASecond) throws InvalidInstructionException;

    boolean shouldTerminate();

    static boolean nextOpcodeIsF000(Processor processor, Memory memory) {
        int currentProgramCounter = processor.getProgramCounter();
        int firstByte = memory.readByte(currentProgramCounter);
        int secondByte = memory.readByte(currentProgramCounter + 1);
        int opcode = (firstByte << 8) | secondByte;
        return opcode == 0xF000;
    }

}
