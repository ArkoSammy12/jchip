package io.github.arkosammy12.jchip.instructions;

import io.github.arkosammy12.jchip.base.ExecutionContext;
import io.github.arkosammy12.jchip.base.Processor;
import io.github.arkosammy12.jchip.util.ConsoleVariant;
import io.github.arkosammy12.jchip.util.InvalidInstructionException;

public class ALUInstruction extends AbstractInstruction {

    private final int firstRegister = this.getSecondNibble();
    private final int type = this.getFourthNibble();
    private final int vX;
    private final int vY;

    public ALUInstruction(int firstByte, int secondByte, ExecutionContext executionContext) {
        super(firstByte, secondByte, executionContext);
        Processor processor = executionContext.getProcessor();
        int secondRegister = this.getThirdNibble();
        this.vX = processor.getRegister(firstRegister);
        this.vY = processor.getRegister(secondRegister);
    }

    @Override
    public void execute() throws InvalidInstructionException {
        Processor processor = this.executionContext.getProcessor();
        ConsoleVariant consoleVariant = this.executionContext.getConsoleVariant();
        switch (type) {
            case 0x0 -> { // Copy to register
                processor.setRegister(firstRegister, vY);
            }
            case 0x1 -> { // Or and register
                int value = (vX | vY) & 0xFF;
                processor.setRegister(firstRegister, value);
                if (consoleVariant == ConsoleVariant.CHIP_8) {
                    processor.setCarry(false);
                }
            }
            case 0x2 -> { // AND and register
                int value = (vX & vY) & 0xFF;
                processor.setRegister(firstRegister, value);
                if (consoleVariant == ConsoleVariant.CHIP_8) {
                    processor.setCarry(false);
                }
            }
            case 0x3 -> { // XOR and register
                int value = (vX ^ vY) & 0xFF;
                processor.setRegister(firstRegister, value);
                if (consoleVariant == ConsoleVariant.CHIP_8) {
                    processor.setCarry(false);
                }
            }
            case 0x4 -> { // Add registers
                int value = vX + vY;
                boolean withCarry = value > 255;
                int maskedValue = value & 0xFF;
                processor.setRegister(firstRegister, maskedValue);
                processor.setCarry(withCarry);
            }
            case 0x5 -> { // Subtract registers (vX - vY)
                int value = (vX - vY) & 0xFF;
                boolean noBorrow = vX >= vY;
                processor.setRegister(firstRegister, value);
                processor.setCarry(noBorrow);
            }
            case 0x6 -> { // Shift right and register
                int operand = consoleVariant == ConsoleVariant.CHIP_8 || consoleVariant == ConsoleVariant.XO_CHIP? vY : vX;
                boolean shiftedOut = (operand & 1) > 0;
                int value = (operand >>> 1) & 0xFF;
                processor.setRegister(firstRegister, value);
                processor.setCarry(shiftedOut);
            }
            case 0x7 -> { // Subtract registers (vY - vX)
                int value = (vY - vX) & 0xFF;
                boolean noBorrow = vY >= vX;
                processor.setRegister(firstRegister, value);
                processor.setCarry(noBorrow);
            }
            case 0xE -> { // Shift left and register
                int operand = consoleVariant == ConsoleVariant.CHIP_8 || consoleVariant == ConsoleVariant.XO_CHIP ? vY : vX;
                boolean shiftedOut = (operand & 128) > 0;
                int value = (operand << 1) & 0xFF;
                processor.setRegister(firstRegister, value);
                processor.setCarry(shiftedOut);
            }
            default -> throw new InvalidInstructionException(this);
        }
    }

}
