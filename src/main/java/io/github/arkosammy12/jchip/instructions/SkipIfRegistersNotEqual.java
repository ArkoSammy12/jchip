package io.github.arkosammy12.jchip.instructions;

import io.github.arkosammy12.jchip.base.ExecutionContext;
import io.github.arkosammy12.jchip.base.Processor;

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
        if (vX != vY) {
            processor.incrementProgramCounter();
        }
    }

}
