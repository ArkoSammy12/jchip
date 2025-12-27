package io.github.arkosammy12.jchip.disassembler;

public record DisassemblerEntry(int address, int length, int bytecode, String text) implements Disassembler.Entry {

    @Override
    public int getInstructionAddress() {
        return this.address();
    }

    @Override
    public int getLength() {
        return this.length();
    }

    @Override
    public int getByteCode() {
        return this.bytecode();
    }

    @Override
    public String getText() {
        return this.text();
    }

}
