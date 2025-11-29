package io.github.arkosammy12.jchip.emulators;

import io.github.arkosammy12.jchip.config.Chip8EmulatorSettings;
import io.github.arkosammy12.jchip.cpu.Chip8XProcessor;
import io.github.arkosammy12.jchip.memory.Chip8XMemory;
import io.github.arkosammy12.jchip.video.Chip8XDisplay;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnknownNullability;

import java.util.Objects;

public class Chip8XEmulator extends Chip8Emulator {

    @Nullable
    private Chip8XProcessor<?> processor;

    @Nullable
    private Chip8XMemory memory;

    @Nullable
    private Chip8XDisplay<?> display;

    public Chip8XEmulator(Chip8EmulatorSettings emulatorSettings) {
        super(emulatorSettings);
    }

    @Override
    @NotNull
    public Chip8XProcessor<?> getProcessor() {
        return Objects.requireNonNull(this.processor);
    }

    @Override
    @NotNull
    public Chip8XDisplay<?> getDisplay() {
        return Objects.requireNonNull(this.display);
    }

    @Override
    @NotNull
    public Chip8XMemory getMemory() {
        return Objects.requireNonNull(this.memory);
    }

    @Override
    @Nullable
    protected Chip8XProcessor<?> createProcessor() {
        this.processor = new Chip8XProcessor<>(this);
        return this.processor;
    }

    @Override
    @Nullable
    protected Chip8XDisplay<?> createDisplay() {
        this.display = new Chip8XDisplay<>(this);
        return this.display;
    }

    @Override
    @Nullable
    protected Chip8XMemory createMemory() {
        this.memory = new Chip8XMemory(this);
        return this.memory;
    }

}
