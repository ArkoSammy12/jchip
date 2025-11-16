package io.github.arkosammy12.jchip.memory;

import io.github.arkosammy12.jchip.emulators.CosmacVipEmulator;

public class ExpandedCosmacVipMemory extends CosmacVipMemory {

    public ExpandedCosmacVipMemory(CosmacVipEmulator emulator) {
        super(emulator);
    }

    @Override
    public int getMemorySize() {
        return 8192;
    }

    @Override
    public int getMemoryBoundsMask() {
        return 0x1FFF;
    }

    @Override
    public int readByte(int address) {
        int actualAddress = this.ma7Latched ? address | 0x8000 : address;
        if (actualAddress >= 0x8000) {
            return super.readByte(address);
        }
        // Mirror the first 4K of on-board RAM after the appended 4K ram expansion board
        if (actualAddress > 0x1FFF) {
            actualAddress &= 0xFFF;
        }
        int value = this.bytes[actualAddress];
        this.dataBus = value;
        return value;
    }

    @Override
    public void writeByte(int address, int value) {
        int actualAddress = this.ma7Latched ? address | 0x8000 : address;
        if (actualAddress >= 0x8000) {
            return;
        }
        // Mirror the first 4K of on-board RAM after the appended 4K ram expansion board
        if (actualAddress > 0x1FFF) {
            actualAddress &= 0xFFF;
        }
        this.dataBus = value;
        this.bytes[actualAddress] = value;
    }

}
