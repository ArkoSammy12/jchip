package io.github.arkosammy12.jchip.hardware;

import io.github.arkosammy12.jchip.util.KeyboardLayout;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;

public class KeyState extends KeyAdapter {

    private final boolean[] keyState = new boolean[16];
    private int waitingKey = -1;
    private boolean terminateEmulator = false;
    private final KeyboardLayout keyboardLayout;

    public KeyState(KeyboardLayout keyboardLayout) {
        this.keyboardLayout = keyboardLayout;
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
            terminateEmulator = true;
        }
        char c = e.getKeyChar();
        int keyCode = this.keyboardLayout.getKeypadHexForChar(c);
        if (keyCode < 0) {
            return;
        }
        this.setKeyPressed(keyCode);
    }

    @Override
    public void keyReleased(KeyEvent e) {
        char c = e.getKeyChar();
        int keyCode = this.keyboardLayout.getKeypadHexForChar(c);
        if (keyCode < 0) {
            return;
        }
        this.setKeyUnpressed(keyCode);
    }

    public boolean isKeyPressed(int hex) {
        return this.keyState[hex];
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
            if (this.keyState[i]) {
                pressedKeys.add(i);
            }
        }
        return pressedKeys;
    }

    private synchronized void setKeyPressed(int keyCode) {
        this.keyState[keyCode] = true;
    }

    private synchronized void setKeyUnpressed(int keyCode) {
        this.keyState[keyCode] = false;
    }

}
