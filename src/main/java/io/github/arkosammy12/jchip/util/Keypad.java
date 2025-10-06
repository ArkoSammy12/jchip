package io.github.arkosammy12.jchip.util;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;

public class Keypad extends KeyAdapter {

    private final KeyboardLayout keyboardLayout;
    private final boolean[] keys = new boolean[16];
    private int waitingKey = -1;
    private boolean terminateEmulator = false;

    public Keypad(KeyboardLayout keyboardLayout) {
        this.keyboardLayout = keyboardLayout;
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
            terminateEmulator = true;
        }
        int hex = this.keyboardLayout.getKeypadHexForChar(e.getKeyChar());
        if (hex > -1) {
            this.setKeypadKeyPressed(hex);
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        int hex = this.keyboardLayout.getKeypadHexForChar(e.getKeyChar());
        if (hex > -1) {
            this.setKeypadKeyUnpressed(hex);
        }
    }

    public synchronized boolean isKeyPressed(int hex) {
        return this.keys[hex];
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

    public boolean shouldTerminate() {
        return this.terminateEmulator;
    }

    public List<Integer> getPressedKeypadKeys() {
        List<Integer> pressedKeys = new ArrayList<>(16);
        for (int i = 0; i < 16; i++) {
            if (this.keys[i]) {
                pressedKeys.add(i);
            }
        }
        return pressedKeys;
    }

    private synchronized void setKeypadKeyPressed(int keyCode) {
        this.keys[keyCode] = true;
    }

    private synchronized void setKeypadKeyUnpressed(int keyCode) {
        this.keys[keyCode] = false;
    }

}
