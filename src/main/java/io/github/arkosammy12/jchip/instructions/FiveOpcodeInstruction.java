package io.github.arkosammy12.jchip.instructions;

import io.github.arkosammy12.jchip.base.ExecutionContext;
import io.github.arkosammy12.jchip.base.Processor;
import io.github.arkosammy12.jchip.util.ConsoleVariant;
import io.github.arkosammy12.jchip.util.InvalidInstructionException;

public class FiveOpcodeInstruction extends AbstractInstruction {

    private final int vX;
    private final int vY;
    private final int type;

    public FiveOpcodeInstruction(int firstByte, int secondByte, ExecutionContext executionContext) {
        super(firstByte, secondByte, executionContext);
        Processor processor = executionContext.getProcessor();
        int firstRegister = this.getSecondNibble();
        int secondRegister = this.getThirdNibble();
        this.vX = processor.getRegister(firstRegister);
        this.vY = processor.getRegister(secondRegister);
        this.type = this.getFourthNibble();
    }

    @Override
    public void execute() throws InvalidInstructionException {
        Processor processor = this.executionContext.getProcessor();
        ConsoleVariant consoleVariant = this.executionContext.getConsoleVariant();
        switch (type) {
            case 0x0 -> { // Skip if registers equal
                if (vX == vY) {
                    processor.incrementProgramCounter();
                }
            }
            case 0x2 -> { // Copy values vX to vY to memory
                if (consoleVariant != ConsoleVariant.XO_CHIP) {
                    break;
                }
                // TODO: Implement
            }
            case 0x3 -> { // Load values vY to vY from memory
                if (consoleVariant != ConsoleVariant.XO_CHIP) {
                    break;
                }
                // TODO: Implement
            }
            default -> throw new InvalidInstructionException(this);
        }
    }

}
