package io.github.arkosammy12.jchip.util.vip;

import io.github.arkosammy12.jchip.emulators.CosmacVipEmulator;
import io.github.arkosammy12.jchip.util.Keypad;

public class CosmacVIPKeypad extends Keypad implements IODevice {

    private final CosmacVipEmulator emulator;
    private int latchedKey = 0;

    public CosmacVIPKeypad(CosmacVipEmulator emulator) {
        super(emulator);
        this.emulator = emulator;
    }

    @Override
    public void cycle() {
        this.emulator.getProcessor().setEF(2, this.isKeyPressed(this.latchedKey));
    }

    @Override
    public boolean isOutputPort(int port)  {
        return port == 2;
    }

    @Override
    public void onOutput(int port, int value) {
        this.latchedKey = value & 0xF;
    }

}
