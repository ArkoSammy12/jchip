package io.github.arkosammy12.jchip.disassembler;

import io.github.arkosammy12.jchip.emulators.SChip11Emulator;
import io.github.arkosammy12.jchip.memory.Bus;

import static io.github.arkosammy12.jchip.cpu.Chip8Processor.*;

public class SChip11Disassembler<E extends SChip11Emulator> extends SChip10Disassembler<E> {

    public SChip11Disassembler(E emulator) {
        super(emulator);
    }

    @Override
    protected String getTextForInstructionAt(int address) {
        Bus bus = this.emulator.getBus();
        int firstByte = bus.getByte(address);
        int secondByte = bus.getByte(address + 1);
        if (firstByte == 0x00) {
            return switch (secondByte) {
                case 0xFB -> "scroll-right";
                case 0xFC -> "scroll-left";
                default -> {
                    if (getY(firstByte, secondByte) == 0xC) {
                        yield "scroll-down " + getNFormatted(firstByte, secondByte);
                    } else {
                        yield super.getTextForInstructionAt(address);
                    }
                }
            };
        } else {
            return super.getTextForInstructionAt(address);
        }
    }

}
