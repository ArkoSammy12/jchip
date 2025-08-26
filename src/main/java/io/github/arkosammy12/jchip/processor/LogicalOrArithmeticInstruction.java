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
        int vX = emulator.getProcessor().getRegisterValue(firstRegister);
        int vY = emulator.getProcessor().getRegisterValue(secondRegister);
        switch (type) {
            case 0x0 -> { // Copy to register
                emulator.getProcessor().setRegisterValue(firstRegister, vY);
            }
            case 0x1 -> { // Or and register
                int value = (vX | vY) & 0xFF;
                emulator.getProcessor().setRegisterValue(firstRegister, value);
                // Reset carry register on bitwise operation. COSMAC CHIP-8 quirk
                emulator.getProcessor().setCarry(false);
            }
            case 0x2 -> { // AND and register
                int value = (vX & vY) & 0xFF;
                emulator.getProcessor().setRegisterValue(firstRegister, value);
                // Reset carry register on bitwise operation. COSMAC CHIP-8 quirk
                emulator.getProcessor().setCarry(false);
            }
            case 0x3 -> { // XOR and register
                int value = (vX ^ vY) & 0xFF;
                emulator.getProcessor().setRegisterValue(firstRegister, value);
                // Reset carry register on bitwise operation. COSMAC CHIP-8 quirk
                emulator.getProcessor().setCarry(false);
            }
            case 0x4 -> { // Add registers
                int value = vX + vY;
                boolean withCarry = value > 255;
                int maskedValue = value & 0xFF;
                emulator.getProcessor().setRegisterValue(firstRegister, maskedValue);
                emulator.getProcessor().setCarry(withCarry);
            }
            case 0x5 -> { // Subtract registers (vX - vY)
                int value = (vX - vY) & 0xFF;
                boolean noBorrow = vX >= vY;
                emulator.getProcessor().setRegisterValue(firstRegister, value);
                emulator.getProcessor().setCarry(noBorrow);
            }
            case 0x6 -> { // Shift right and register
                // Copying vY into Vx then shifting vX is a quirk. COSMAC CHIP-8
                boolean shiftedOut = (vY & 1) > 0;
                int value = (vY >>> 1) & 0xFF;
                emulator.getProcessor().setRegisterValue(firstRegister, value);
                emulator.getProcessor().setCarry(shiftedOut);
            }
            case 0x7 -> { // Subtract registers (vY - vX)
                int value = (vY - vX) & 0xFF;
                boolean noBorrow = vY >= vX;
                emulator.getProcessor().setRegisterValue(firstRegister, value);
                emulator.getProcessor().setCarry(noBorrow);
            }
            case 0xE -> { // Shift left and register
                // Copying vY into Vx then shifting vX is a quirk. COSMAC CHIP-8
                boolean shiftedOut = (vY & 128) > 0;
                int value = (vY << 1) & 0xFF;
                emulator.getProcessor().setRegisterValue(firstRegister, value);
                emulator.getProcessor().setCarry(shiftedOut);
            }
        }
    }

}
