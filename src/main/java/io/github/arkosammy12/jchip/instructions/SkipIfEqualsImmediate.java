package io.github.arkosammy12.jchip.instructions;

import io.github.arkosammy12.jchip.base.ExecutionContext;
import io.github.arkosammy12.jchip.base.Memory;
import io.github.arkosammy12.jchip.base.Processor;
import io.github.arkosammy12.jchip.util.ConsoleVariant;

public class SkipIfEqualsImmediate extends AbstractInstruction {

    private final int operand = this.getSecondByte();
    private final int vX;

    public SkipIfEqualsImmediate(int firstByte, int secondByte, ExecutionContext executionContext) {
        super(firstByte, secondByte, executionContext);
        Processor processor = executionContext.getProcessor();
        int register = this.getSecondNibble();
        this.vX = processor.getRegister(register);
    }

    @Override
    public void execute() {
        Processor processor = executionContext.getProcessor();
        Memory memory = executionContext.getMemory();
        ConsoleVariant consoleVariant = executionContext.getConsoleVariant();
        if (operand == vX) {
            if (consoleVariant == ConsoleVariant.XO_CHIP && Processor.nextOpcodeIsF000(processor, memory)) {
                processor.incrementProgramCounter();
            }
            processor.incrementProgramCounter();
        }
    }

}
