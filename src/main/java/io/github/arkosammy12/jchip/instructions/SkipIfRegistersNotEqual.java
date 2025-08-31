package io.github.arkosammy12.jchip.instructions;

import io.github.arkosammy12.jchip.base.ExecutionContext;
import io.github.arkosammy12.jchip.base.Memory;
import io.github.arkosammy12.jchip.base.Processor;
import io.github.arkosammy12.jchip.util.ConsoleVariant;

public class SkipIfRegistersNotEqual extends AbstractInstruction {

    private final int vX;
    private final int vY;

    public SkipIfRegistersNotEqual(int firstByte, int secondByte, ExecutionContext executionContext) {
        super(firstByte, secondByte, executionContext);
        Processor processor = executionContext.getProcessor();
        int firstRegister = this.getSecondNibble();
        int secondRegister = this.getThirdNibble();
        this.vX = processor.getRegister(firstRegister);
        this.vY = processor.getRegister(secondRegister);
    }

    @Override
    public void execute() {
        Processor processor = executionContext.getProcessor();
        Memory memory = executionContext.getMemory();
        ConsoleVariant consoleVariant = executionContext.getConsoleVariant();
        if (vX != vY) {
            boolean nextOpcodeIsF000 = Processor.nextOpcodeIsF000(processor, memory);
            processor.incrementProgramCounter();
            if (consoleVariant == ConsoleVariant.XO_CHIP && nextOpcodeIsF000) {
                processor.incrementProgramCounter();
            }
        }
    }

}
