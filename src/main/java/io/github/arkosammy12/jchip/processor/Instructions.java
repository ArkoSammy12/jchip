package io.github.arkosammy12.jchip.processor;

import java.util.function.BiFunction;

public enum Instructions {
    ZERO_OPCODE(0x0, ZeroOpcodeInstruction::new),
    JUMP(0x1, JumpInstruction::new),
    CALL(0x2, CallInstruction::new),
    SKIP_IF_EQUALS_IMMEDIATE(0x3, SkipIfEqualsImmediate::new),
    SKIP_IF_NOT_EQUALS_IMMEDIATE(0x4, SkipIfNotEqualsImmediate::new),
    SKIP_IF_REGISTERS_EQUAL(0x5, SkipIfRegistersEqual::new),
    SET_REGISTER(0x6, SetRegister::new),
    ADD_TO_REGISTER(0x7, AddToRegister::new),
    LOGICAL_OR_ARITHMETIC_INSTRUCTION(0x8, LogicalOrArithmeticInstruction::new),
    SKIP_IF_REGISTERS_NOT_EQUAL(0x9, SkipIfRegistersNotEqual::new),
    SET_INDEX_REGISTER(0xA, SetIndexInstruction::new),
    JUMP_WITH_OFFSET(0xB, JumpWithOffsetInstruction::new),
    GENERATE_RANDOM(0xC, RandomNumberInstruction::new),
    DISPLAY_INSTRUCTION(0xD, DisplayInstruction::new),
    SKIP_IF_KEY(0xE, SkipIfKey::new),
    F_OPCODE_INSTRUCTION(0xF, FOpcodeInstruction::new);

    private final int opcode;
    private final BiFunction<Integer, Integer, Instruction> instructionSupplier;

    Instructions(int opcode, BiFunction<Integer, Integer, Instruction> instructionSupplier) {
        this.opcode = opcode;
        this.instructionSupplier = instructionSupplier;
    }

    public static Instruction decodeBytes(int firstByte, int secondByte) {
        int opcode = (firstByte  & 0xF0) >> 4;
        for (Instructions instruction : Instructions.values()) {
            if (opcode == instruction.opcode) {
                return instruction.instructionSupplier.apply(firstByte, secondByte);
            }
        }
        throw new IllegalArgumentException();
    }

}
