package io.github.arkosammy12.jchip.io;

import com.googlecode.lanterna.input.KeyStroke;
import io.github.arkosammy12.jchip.Emulator;
import io.github.arkosammy12.jchip.Utils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class KeyState {

    private final boolean[] keyState = new boolean[16];
    private int waitingKey = -1;

    public void setKeyPressed(int keyCode) {
        this.keyState[keyCode] = true;
    }

    public void setKeyPressed(char c) {
        this.keyState[Utils.getIntegerForCharacter(c)] = true;
    }

    public void setKeyUnpressed(int keyCode) {
        this.keyState[keyCode] = false;
    }

    public void setKeyUnpressed(char c) {
        this.keyState[Utils.getIntegerForCharacter(c)] = false;
    }

    public boolean isKeyPressed(int keyCode) {
        return this.keyState[keyCode];
    }

    public void updateKeyState(Emulator emulator) throws IOException {
        //Arrays.fill(this.keyState, false);
        KeyStroke keyStroke;
        while ((keyStroke = emulator.getEmulatorScreen().pollInput()) != null) {
            char c = keyStroke.getCharacter();
            int keyCode = Utils.getIntegerForCharacter(c);
            if (keyCode < 0) {
                continue;
            }
            this.setKeyPressed(keyCode);
        }
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

    public int getWaitingKey() { return waitingKey; }
    public void setWaitingKey(int key) { this.waitingKey = key; }
    public boolean isKeyWaiting(int keyCode) {
        return keyCode == this.waitingKey;
    }
    public void resetWaitingKey() {
        this.waitingKey = -1;
    }

}
