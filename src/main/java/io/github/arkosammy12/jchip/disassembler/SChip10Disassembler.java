package io.github.arkosammy12.jchip.disassembler;

import io.github.arkosammy12.jchip.emulators.SChip10Emulator;
import io.github.arkosammy12.jchip.memory.Bus;

import static io.github.arkosammy12.jchip.cpu.Chip8Processor.getNNN;

public class SChip10Disassembler<E extends SChip10Emulator> extends Chip8Disassembler<E> {

    public SChip10Disassembler(E emulator) {
        super(emulator);
    }

    @Override
    protected String getTextForEntry(Entry entry) {
        int address = entry.getInstructionAddress();
        Bus bus = this.emulator.getBus();
        int firstByte = bus.getByte(address);
        int secondByte = bus.getByte(address + 1);
        return switch (firstByte >>> 4) {
            case 0x0 -> switch (getNNN(firstByte, secondByte)) {
                case 0x0FD -> "exit";
                case 0x0FE -> "lores";
                case 0x0FF -> "hires";
                default -> super.getTextForEntry(entry);
            };
            case 0xF -> switch (secondByte) {
                case 0x30 -> "i := bighex v" + getXFormatted(firstByte, secondByte);
                case 0x75 -> "saveflags v" + getXFormatted(firstByte, secondByte);
                case 0x85 -> "loadflags v" + getXFormatted(firstByte, secondByte);
                default -> super.getTextForEntry(entry);
            };
            default -> super.getTextForEntry(entry);
        };
    }

}
