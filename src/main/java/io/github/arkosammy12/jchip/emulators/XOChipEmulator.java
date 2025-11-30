package io.github.arkosammy12.jchip.emulators;

import io.github.arkosammy12.jchip.config.Chip8EmulatorSettings;
import io.github.arkosammy12.jchip.cpu.XOChipProcessor;
import io.github.arkosammy12.jchip.memory.XOChipBus;
import io.github.arkosammy12.jchip.video.XOChipDisplay;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class XOChipEmulator extends SChipEmulator {

    @Nullable
    private XOChipProcessor<?> processor;

    @Nullable
    private XOChipBus bus;

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
    public XOChipBus getBus() {
        return Objects.requireNonNull(this.bus);
    }

    @Override
    protected XOChipProcessor<?> createProcessor() {
        this.processor = new XOChipProcessor<>(this);
        return this.processor;
    }

    @Override
    protected XOChipDisplay<?> createDisplay() {
        this.display = new XOChipDisplay<>(this);
        return this.display;
    }

    @Override
    protected XOChipBus createBus() {
        this.bus = new XOChipBus(this);
        return this.bus;
    }

}
