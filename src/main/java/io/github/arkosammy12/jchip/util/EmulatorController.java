package io.github.arkosammy12.jchip.util;

import org.graalvm.collections.Pair;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedDeque;

public class EmulatorController extends KeyAdapter {

    private final Map<Integer, List<Runnable>> controllers = new HashMap<>();
    private final Queue<Runnable> controllerBuffer = new ConcurrentLinkedDeque<>();
    private final Set<Integer> pressedKeys = new HashSet<>();

    private EmulatorController(Builder builder) {
        for (Pair<Integer, Runnable> controller : builder.getControllers()) {
            int keyCode = controller.getLeft();
            Runnable action = controller.getRight();
            if (this.controllers.containsKey(keyCode)) {
                this.controllers.get(keyCode).add(action);
            } else {
                this.controllers.put(keyCode, new ArrayList<>(List.of(action)));
            }
        }
    }

    @Override
    public synchronized void keyPressed(KeyEvent e) {
        int keyCode = e.getKeyCode();
        if (!this.pressedKeys.add(keyCode)) {
            return;
        }
        this.controllerBuffer.addAll(this.controllers.getOrDefault(keyCode, Collections.emptyList()));
    }


    @Override
    public synchronized void keyReleased(KeyEvent e) {
        this.pressedKeys.remove(e.getKeyCode());
    }

    public void pollControllers() {
        while (!this.controllerBuffer.isEmpty()) {
            this.controllerBuffer.poll().run();
        }
    }

    public static class Builder {

        private final List<Pair<Integer, Runnable>> controllers = new ArrayList<>();

        public Builder withController(Integer keycode, Runnable controller) {
            this.controllers.add(Pair.create(keycode, controller));
            return this;
        }

        public Builder withControllers(Collection<Integer> keycodes, Runnable controller) {
            for (Integer keycode : keycodes) {
                this.controllers.add(Pair.create(keycode, controller));
            }
            return this;
        }

        public Builder withControllers(Runnable controller, Integer... keycodes) {
            for (Integer keycode : keycodes) {
                this.controllers.add(Pair.create(keycode, controller));
            }
            return this;
        }

        public EmulatorController build() {
            return new EmulatorController(this);
        }

        private List<Pair<Integer, Runnable>> getControllers() {
            return this.controllers;
        }


    }


}

