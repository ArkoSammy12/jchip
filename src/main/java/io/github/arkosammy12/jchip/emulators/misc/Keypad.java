package io.github.arkosammy12.jchip.emulators.misc;

import io.github.arkosammy12.jchip.main.Jchip;
import io.github.arkosammy12.jchip.emulators.Emulator;
import io.github.arkosammy12.jchip.util.KeyboardLayout;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;

public class Keypad extends KeyAdapter {

    private final Jchip jchip;
    private final boolean[] keys = new boolean[16];
    private int waitingKey = -1;

    public Keypad(Emulator emulator) {
        this.jchip = emulator.getEmulatorSettings().getJchip();
    }

    @Override
    public void keyPressed(KeyEvent e) {
        int hex = this.jchip.getMainWindow().getSettingsBar().getKeyboardLayout().orElse(KeyboardLayout.QWERTY).getKeypadHexForKeyCode(e.getKeyCode());
        if (hex > -1) {
            this.setKeypadKeyPressed(hex);
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        int hex = this.jchip.getMainWindow().getSettingsBar().getKeyboardLayout().orElse(KeyboardLayout.QWERTY).getKeypadHexForKeyCode(e.getKeyCode());
        if (hex > -1) {
            this.setKeypadKeyUnpressed(hex);
        }
    }

    public synchronized boolean isKeyPressed(int hex) {
        return this.keys[hex];
    }

    public synchronized List<Integer> getPressedKeypadKeys() {
        List<Integer> pressedKeys = new ArrayList<>(16);
        for (int i = 0; i < 16; i++) {
            if (this.keys[i]) {
                pressedKeys.add(i);
            }
        }
        return pressedKeys;
    }

    public void setWaitingKeypadKey(int hex) {
        this.waitingKey = hex;
    }

    public int getWaitingKeypadKey() {
        return waitingKey;
    }

    public void resetWaitingKeypadKey() {
        this.waitingKey = -1;
    }

    private synchronized void setKeypadKeyPressed(int keyCode) {
        this.keys[keyCode] = true;
    }

    private synchronized void setKeypadKeyUnpressed(int keyCode) {
        this.keys[keyCode] = false;
    }

}
