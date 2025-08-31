package io.github.arkosammy12.jchip.instructions;

import io.github.arkosammy12.jchip.base.ExecutionContext;
import io.github.arkosammy12.jchip.base.Processor;

public class GetRandomNumber extends AbstractInstruction {

    private final int register = this.getSecondNibble();
    private final int operand = this.getSecondByte();

    public GetRandomNumber(int firstByte, int secondByte, ExecutionContext executionContext) {
        super(firstByte, secondByte, executionContext);
    }

    @Override
    public void execute() {
        Processor processor = this.executionContext.getProcessor();
        int random = processor.getRandom().nextInt();
        int value = (random & operand) & 0xFF;
        processor.setRegister(register, value);
    }

}
