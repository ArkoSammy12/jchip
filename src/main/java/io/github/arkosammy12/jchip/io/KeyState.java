package io.github.arkosammy12.jchip.io;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;

public class KeyState extends KeyAdapter {

    private final boolean[] keyState = new boolean[16];
    private int waitingKey = -1;
    private boolean terminateEmulator = false;

    @Override
    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
            terminateEmulator = true;
        }
        char c = e.getKeyChar();
        int keyCode = getIntegerForCharacterQWERTY(c);
        if (keyCode < 0) {
            return;
        }
        this.setKeyPressed(keyCode);
    }

    @Override
    public void keyReleased(KeyEvent e) {
        char c = e.getKeyChar();
        int keyCode = getIntegerForCharacterQWERTY(c);
        if (keyCode < 0) {
            return;
        }
        this.setKeyUnpressed(keyCode);
    }

    public boolean isKeyPressed(int keyCode) {
        return this.keyState[keyCode];
    }

    public void setWaitingKey(int key) {
        this.waitingKey = key;
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

    public static int getIntegerForCharacterQWERTY(char c) {
        return switch (c) {
            case 'x' -> 0x0;
            case '1' -> 0x1;
            case '2' -> 0x2;
            case '3' -> 0x3;
            case 'q' -> 0x4;
            case 'w' -> 0x5;
            case 'e' -> 0x6;
            case 'a' -> 0x7;
            case 's' -> 0x8;
            case 'd' -> 0x9;
            case 'z' -> 0xA;
            case 'c' -> 0xB;
            case '4' -> 0xC;
            case 'r' -> 0xD;
            case 'f' -> 0xE;
            case 'v' -> 0xF;
            default -> -1;
        };
    }

    public static int getIntegerForCharacterAZERTY(char c) {
        return switch (c) {
            case 'x' -> 0x0;
            case '1' -> 0x1;
            case '2' -> 0x2;
            case '3' -> 0x3;
            case 'a' -> 0x4;
            case 'z' -> 0x5;
            case 'e' -> 0x6;
            case 'q' -> 0x7;
            case 's' -> 0x8;
            case 'd' -> 0x9;
            case 'w' -> 0xA;
            case 'c' -> 0xB;
            case '4' -> 0xC;
            case 'r' -> 0xD;
            case 'f' -> 0xE;
            case 'v' -> 0xF;
            default -> -1;
        };
    }

    public static int getIntegerForCharacterColemak(char c) {
        return switch (c) {
            case 'x' -> 0x0;
            case '1' -> 0x1;
            case '2' -> 0x2;
            case '3' -> 0x3;
            case 'q' -> 0x4;
            case 'w' -> 0x5;
            case 'f' -> 0x6;
            case 'a' -> 0x7;
            case 'r' -> 0x8;
            case 's' -> 0x9;
            case 'z' -> 0xA;
            case 'c' -> 0xB;
            case '4' -> 0xC;
            case 'p' -> 0xD;
            case 't' -> 0xE;
            case 'v' -> 0xF;
            default -> -1;
        };
    }

}
