package io.github.arkosammy12.jchip.instructions;

import io.github.arkosammy12.jchip.base.ExecutionContext;
import io.github.arkosammy12.jchip.base.Processor;

public class SetRegisterImmediate extends AbstractInstruction {

    private final int register = this.getSecondNibble();
    private final int value = this.getSecondByte();

    public SetRegisterImmediate(int firstByte, int secondByte, ExecutionContext executionContext) {
        super(firstByte, secondByte, executionContext);
    }

    @Override
    public void execute() {
        Processor processor = this.executionContext.getProcessor();
        processor.setRegister(register, value);
    }

}
