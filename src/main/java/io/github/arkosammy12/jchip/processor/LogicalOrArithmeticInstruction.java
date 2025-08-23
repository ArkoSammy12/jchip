package io.github.arkosammy12.jchip.processor;

import io.github.arkosammy12.jchip.Emulator;

public class LogicalOrArithmeticInstruction extends Instruction {

    public LogicalOrArithmeticInstruction(int firstByte, int secondByte) {
        super(firstByte, secondByte);
    }

    @Override
    public void execute(Emulator emulator) {
        int type = this.getFourthNibble();
        int firstRegister = this.getSecondNibble();
        int secondRegister = this.getThirdNibble();
        int vx = emulator.getProcessor().getByteInRegister(firstRegister);
        int vy = emulator.getProcessor().getByteInRegister(secondRegister);
        switch (type) {
            case 0x0 -> { // Copy to register
                emulator.getProcessor().setByteInRegister(firstRegister, vy);
            }
            case 0x1 -> { // Or and register
                int value = (vx | vy) & 0xFF;
                emulator.getProcessor().setByteInRegister(firstRegister, value);
            }
            case 0x2 -> { // AND and register
                int value = (vx & vy) & 0xFF;
                emulator.getProcessor().setByteInRegister(firstRegister, value);
            }
            case 0x3 -> { // XOR and register
                int value = (vx ^ vy) & 0xFF;
                emulator.getProcessor().setByteInRegister(firstRegister, value);
            }
            case 0x4 -> { // Add registers
                int value = vx + vy;
                int carry = value > 255 ? 1 : 0;
                int maskedValue = value & 0xFF;
                emulator.getProcessor().setByteInRegister(firstRegister, maskedValue);
                emulator.getProcessor().setByteInRegister(0xF, carry);
            }
            case 0x5 -> { // Subtract registers (vx - vy)
                int value = (vx - vy) & 0xFF;
                int carry = vx >= vy ? 1 : 0;
                emulator.getProcessor().setByteInRegister(firstRegister, value);
                emulator.getProcessor().setByteInRegister(0xF, carry);
            }
            case 0x6 -> { // Shift right and register
                int shiftedOut = (vy & 1) > 0 ? 1 : 0;
                int value = (vy >>> 1) & 0xFF;
                emulator.getProcessor().setByteInRegister(firstRegister, value);
                emulator.getProcessor().setByteInRegister(0xF, shiftedOut);
            }
            case 0x7 -> { // Subtract registers (vy - vx)
                int value = (vy - vx) & 0xFF;
                int carry = vy >= vx ? 1 : 0;
                emulator.getProcessor().setByteInRegister(firstRegister, value);
                emulator.getProcessor().setByteInRegister(0xF, carry);
            }
            case 0xE -> { // Shift left and register
                int shiftedOut = (vy & 128) > 0 ? 1 : 0;
                int value = (vy << 1) & 0xFF;
                emulator.getProcessor().setByteInRegister(firstRegister, value);
                emulator.getProcessor().setByteInRegister(0xF, shiftedOut);
            }
        }
    }

}
