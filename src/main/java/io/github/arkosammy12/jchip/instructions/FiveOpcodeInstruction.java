package io.github.arkosammy12.jchip.instructions;

import io.github.arkosammy12.jchip.base.ExecutionContext;
import io.github.arkosammy12.jchip.base.Memory;
import io.github.arkosammy12.jchip.base.Processor;
import io.github.arkosammy12.jchip.util.ConsoleVariant;
import io.github.arkosammy12.jchip.util.InvalidInstructionException;

public class FiveOpcodeInstruction extends AbstractInstruction {

    private final int firstRegister;
    private final int secondRegister;
    private final int vX;
    private final int vY;
    private final int type;

    public FiveOpcodeInstruction(int firstByte, int secondByte, ExecutionContext executionContext) {
        super(firstByte, secondByte, executionContext);
        Processor processor = executionContext.getProcessor();
        this.firstRegister = this.getSecondNibble();
        this.secondRegister = this.getThirdNibble();
        this.vX = processor.getRegister(firstRegister);
        this.vY = processor.getRegister(secondRegister);
        this.type = this.getFourthNibble();
    }

    @Override
    public void execute() throws InvalidInstructionException {
        Processor processor = this.executionContext.getProcessor();
        Memory memory = this.executionContext.getMemory();
        ConsoleVariant consoleVariant = this.executionContext.getConsoleVariant();
        switch (type) {
            case 0x0 -> { // Skip if registers equal
                if (vX == vY) {
                    boolean nextOpcodeIsF000 = Processor.nextOpcodeIsF000(processor, memory);
                    processor.incrementProgramCounter();
                    if (consoleVariant == ConsoleVariant.XO_CHIP && nextOpcodeIsF000) {
                        processor.incrementProgramCounter();
                    }
                }
            }
            case 0x2 -> { // Copy values vX to vY to memory
                if (consoleVariant != ConsoleVariant.XO_CHIP) {
                    throw new InvalidInstructionException(this, consoleVariant);
                }
                int currentIndexPointer = processor.getIndexRegister();
                boolean iterateInReverse = firstRegister > secondRegister;
                for (int i = firstRegister, j = 0; iterateInReverse ? i >= secondRegister : i <= secondRegister; j++) {
                    int registerValue = processor.getRegister(i);
                    memory.storeByte(currentIndexPointer + j, registerValue);
                    i = iterateInReverse ? i - 1 : i + 1;
                }
            }
            case 0x3 -> { // Load values vY to vY from memory
                if (consoleVariant != ConsoleVariant.XO_CHIP) {
                    throw new InvalidInstructionException(this, consoleVariant);
                }
                int currentIndexPointer = processor.getIndexRegister();
                boolean iterateInReverse = firstRegister > secondRegister;
                for (int i = firstRegister, j = 0; iterateInReverse ? i >= secondRegister : i <= secondRegister; j++) {
                    int memoryValue = memory.readByte(currentIndexPointer + j);
                    processor.setRegister(i, memoryValue);
                    i = iterateInReverse ? i - 1 : i + 1;
                }
            }
            default -> throw new InvalidInstructionException(this);
        }
    }

}
