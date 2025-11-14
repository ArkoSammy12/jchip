package io.github.arkosammy12.jchip.util;

import io.github.arkosammy12.jchip.config.EmulatorSettings;
import io.github.arkosammy12.jchip.emulators.CosmacVipEmulator;
import io.github.arkosammy12.jchip.ui.IODevice;

public class CosmacVIPKeypad extends Keypad implements IODevice {

    private final CosmacVipEmulator emulator;
    private int latchedKey = 0;

    public CosmacVIPKeypad(EmulatorSettings emulatorSettings, CosmacVipEmulator emulator) {
        super(emulatorSettings);
        this.emulator = emulator;
    }

    @Override
    public void cycle() {
        this.emulator.getProcessor().setEF(2, this.isKeyPressed(this.latchedKey));
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
        this.latchedKey = value & 0xF;
    }

    @Override
    public int onInput() {
        return 0;
    }
}
