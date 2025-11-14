package io.github.arkosammy12.jchip.util;

import io.github.arkosammy12.jchip.config.EmulatorSettings;
import io.github.arkosammy12.jchip.emulators.CosmacVipEmulator;
import io.github.arkosammy12.jchip.ui.IODevice;

public class CosmacVIPKeypad extends Keypad implements IODevice {

    private final CosmacVipEmulator emulator;
    private int pressedKey = -1;

    public CosmacVIPKeypad(EmulatorSettings emulatorSettings, CosmacVipEmulator emulator) {
        super(emulatorSettings);
        this.emulator = emulator;
    }

    @Override
    public void cycle() {
        if (pressedKey >= 0 && !this.isKeyPressed(pressedKey)) {
            this.emulator.getProcessor().setEF(2, false);
            pressedKey = -1;
        }
    }

    @Override
    public DmaStatus getDmaStatus() {
        return DmaStatus.NONE;
    }

    @Override
    public boolean isInterrupting() {
        return false;
    }

    @Override
    public void doDmaOut(int value) {

    }

    @Override
    public int doDmaIn() {
        return 0;
    }

    @Override
    public void onOutput(int value) {
        if (this.isKeyPressed(value & 0xF)) {
            this.emulator.getProcessor().setEF(2, true);
            this.pressedKey = value & 0xF;
        }
    }

    @Override
    public int onInput() {
        return 0;
    }
}
