package io.github.arkosammy12.jchip.util;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;

public class Keypad extends KeyAdapter {

    private final boolean[] keys = new boolean[16];
    private int waitingKey = -1;
    private boolean terminateEmulator = false;
    private final KeyboardLayout keyboardLayout;

    public Keypad(KeyboardLayout keyboardLayout) {
        this.keyboardLayout = keyboardLayout;
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
            terminateEmulator = true;
        }
        int keyCode = this.keyboardLayout.getKeypadHexForChar(e.getKeyChar());
        if (keyCode < 0) {
            return;
        }
        this.setKeyPressed(keyCode);
    }

    @Override
    public void keyReleased(KeyEvent e) {
        int keyCode = this.keyboardLayout.getKeypadHexForChar(e.getKeyChar());
        if (keyCode < 0) {
            return;
        }
        this.setKeyUnpressed(keyCode);
    }

    public synchronized boolean isKeyPressed(int hex) {
        return this.keys[hex];
    }

    public void setWaitingKey(int hex) {
        this.waitingKey = hex;
    }

    public int getWaitingKey() {
        return waitingKey;
    }

    public void resetWaitingKey() {
        this.waitingKey = -1;
    }

    public boolean shouldTerminate() {
        return this.terminateEmulator;
    }

    public List<Integer> getPressedKeys() {
        List<Integer> pressedKeys = new ArrayList<>(16);
        for (int i = 0; i < 16; i++) {
            if (this.keys[i]) {
                pressedKeys.add(i);
            }
        }
        return pressedKeys;
    }

    private synchronized void setKeyPressed(int keyCode) {
        this.keys[keyCode] = true;
    }

    private synchronized void setKeyUnpressed(int keyCode) {
        this.keys[keyCode] = false;
    }

}
