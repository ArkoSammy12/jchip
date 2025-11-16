package io.github.arkosammy12.jchip.emulators;

import io.github.arkosammy12.jchip.config.Chip8EmulatorSettings;
import io.github.arkosammy12.jchip.cpu.XOChipProcessor;
import io.github.arkosammy12.jchip.memory.XOChipMemory;
import io.github.arkosammy12.jchip.video.XOChipDisplay;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnknownNullability;

import java.util.Objects;

public class XOChipEmulator extends SChipEmulator {

    @Nullable
    private XOChipProcessor<?> processor;

    @Nullable
    private XOChipMemory memory;

    @Nullable
    private XOChipDisplay<?> display;

    public XOChipEmulator(Chip8EmulatorSettings emulatorSettings) {
        super(emulatorSettings, true);
    }

    @Override
    @NotNull
    public XOChipProcessor<?> getProcessor() {
        return Objects.requireNonNull(this.processor);
    }

    @Override
    @NotNull
    public XOChipDisplay<?> getDisplay() {
        return Objects.requireNonNull(this.display);
    }

    @Override
    @NotNull
    public XOChipMemory getMemory() {
        return Objects.requireNonNull(this.memory);
    }

    @Override
    @Nullable
    protected XOChipProcessor<?> createProcessor() {
        this.processor = new XOChipProcessor<>(this);
        return null;
    }

    @Override
    @Nullable
    protected XOChipDisplay<?> createDisplay() {
        this.display = new XOChipDisplay<>(this);
        return null;
    }

    @Override
    @Nullable
    protected XOChipMemory createMemory() {
        this.memory = new XOChipMemory(this);
        return null;
    }

}
