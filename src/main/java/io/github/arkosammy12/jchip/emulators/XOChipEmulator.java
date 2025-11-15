package io.github.arkosammy12.jchip.emulators;

import io.github.arkosammy12.jchip.config.Chip8EmulatorSettings;
import io.github.arkosammy12.jchip.cpu.XOChipProcessor;
import io.github.arkosammy12.jchip.memory.XOChipMemory;
import io.github.arkosammy12.jchip.video.XOChipDisplay;

public class XOChipEmulator extends SChipEmulator {

    private XOChipProcessor<?> processor;
    private XOChipMemory memory;
    private XOChipDisplay<?> display;

    public XOChipEmulator(Chip8EmulatorSettings emulatorSettings) {
        super(emulatorSettings, true);
    }

    @Override
    public XOChipProcessor<?> getProcessor() {
        return this.processor;
    }

    @Override
    public XOChipMemory getMemory() {
        return this.memory;
    }

    @Override
    public XOChipDisplay<?> getDisplay() {
        return this.display;
    }

    protected void initializeDisplay() {
        this.display = new XOChipDisplay<>(this);
    }

    protected void initializeMemory() {
        this.memory = new XOChipMemory(this);
        this.memory.loadFont(this.getEmulatorSettings().getHexSpriteFont());
    }

    protected void initializeProcessor() {
        this.processor = new XOChipProcessor<>(this);
    }

}
