package io.github.arkosammy12.jchip.processor;

import io.github.arkosammy12.jchip.Emulator;

public class JumpWithOffsetInstruction extends Instruction {

    public JumpWithOffsetInstruction(int firstByte, int secondByte) {
        super(firstByte, secondByte);
    }

    @Override
    public void execute(Emulator emulator) {
        int memoryAddress = this.getMemoryAddress();
        // Using v0 as the offset instead of jumping to XNN + VX is a quirk. COSMAC CHIP-8
        int v0 = emulator.getProcessor().getRegisterValue(0x0);
        int jumpAddress = memoryAddress + v0;
        emulator.getProcessor().setProgramCounter(jumpAddress);
    }

}
