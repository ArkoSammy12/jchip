package io.github.arkosammy12.jchip.emulators;

import io.github.arkosammy12.jchip.config.Chip8EmulatorSettings;
import io.github.arkosammy12.jchip.cpu.XOChipProcessor;
import io.github.arkosammy12.jchip.memory.XOChipMemory;
import io.github.arkosammy12.jchip.video.XOChipDisplay;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.UnknownNullability;

public class XOChipEmulator extends SChipEmulator {

    @UnknownNullability
    private XOChipProcessor<?> processor;

    @UnknownNullability
    private XOChipMemory memory;

    @UnknownNullability
    private XOChipDisplay<?> display;

    public XOChipEmulator(Chip8EmulatorSettings emulatorSettings) {
        super(emulatorSettings, true);
    }

    @Override
    public @NotNull XOChipProcessor<?> getProcessor() {
        return this.processor;
    }

    @Override
    public @NotNull XOChipMemory getMemory() {
        return this.memory;
    }

    @Override
    public @NotNull XOChipDisplay<?> getDisplay() {
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
