package io.github.arkosammy12.jchip.io;

import java.util.ArrayList;
import java.util.List;

public class KeyState {

    private final boolean[] keyState = new boolean[16];
    private int waitingKey = -1;

    public synchronized void setKeyPressed(int keyCode) {
        this.keyState[keyCode] = true;
    }

    public synchronized void setKeyUnpressed(int keyCode) {
        this.keyState[keyCode] = false;
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

    public List<Integer> getPressedKeys() {
        List<Integer> pressedKeys = new ArrayList<>(16);
        for (int i = 0; i < 16; i++) {
            if (this.keyState[i]) {
                pressedKeys.add(i);
            }
        }
        return pressedKeys;
    }

}
