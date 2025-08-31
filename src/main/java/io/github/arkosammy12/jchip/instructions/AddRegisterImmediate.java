package io.github.arkosammy12.jchip.instructions;

import io.github.arkosammy12.jchip.base.ExecutionContext;

public class AddRegisterImmediate extends AbstractInstruction {

    private final int register = this.getSecondNibble();
    private final int addend = this.getSecondByte();
    private final int vX;

    public AddRegisterImmediate(int firstByte, int secondByte, ExecutionContext executionContext) {
        super(firstByte, secondByte, executionContext);
        this.vX = executionContext.getProcessor().getRegister(register);
    }

    @Override
    public void execute() {
        int value = (vX + addend) & 0xFF;
        this.executionContext.getProcessor().setRegister(register, value);
    }
}
