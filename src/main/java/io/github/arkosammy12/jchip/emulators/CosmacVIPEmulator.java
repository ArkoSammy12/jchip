package io.github.arkosammy12.jchip.emulators;

import io.github.arkosammy12.jchip.base.*;
import io.github.arkosammy12.jchip.cpu.CDP1802;
import io.github.arkosammy12.jchip.util.*;
import io.github.arkosammy12.jchip.video.CosmacVIPDisplay;

public class CosmacVIPEmulator implements Emulator {

    private static final int CYCLES_PER_FRAME = 3668;

    private final CDP1802 processor;
    private final CosmacVIPMemory memory;
    private final CosmacVIPDisplay display;
    private final Keypad keypad;

    public CosmacVIPEmulator(EmulatorConfig emulatorConfig) {
        int[] program = emulatorConfig.getRom();
        this.processor = new CDP1802(this);
        this.memory = new CosmacVIPMemory(program);
        this.keypad = new Keypad(emulatorConfig.getKeyboardLayout());
        this.display = new CosmacVIPDisplay(emulatorConfig, keypad);
    }

    @Override
    public Processor getProcessor() {
        return this.processor;
    }

    @Override
    public CosmacVIPMemory getMemory() {
        return this.memory;
    }

    @Override
    public Display getDisplay() {
        return this.display;
    }

    @Override
    public Keypad getKeyState() {
        return this.keypad;
    }

    @Override
    public SoundSystem getSoundSystem() {
        return null;
    }

    @Override
    public Chip8Variant getChip8Variant() {
        return null;
    }

    @Override
    public EmulatorConfig getEmulatorConfig() {
        return null;
    }

    @Override
    public void tick() throws InvalidInstructionException {

    }

    @Override
    public void terminate() {

    }

    @Override
    public boolean isTerminated() {
        return false;
    }

    @Override
    public void close() throws Exception {

    }
}
