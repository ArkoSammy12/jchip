package io.github.arkosammy12.jchip.emulators;

import io.github.arkosammy12.jchip.config.Chip8EmulatorSettings;
import io.github.arkosammy12.jchip.memory.MegaChipMemory;
import io.github.arkosammy12.jchip.cpu.Chip8Processor;
import io.github.arkosammy12.jchip.cpu.MegaChipProcessor;
import io.github.arkosammy12.jchip.sound.MegaChipSoundSystem;
import io.github.arkosammy12.jchip.video.MegaChipDisplay;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.UnknownNullability;

import static io.github.arkosammy12.jchip.cpu.Chip8Processor.isSet;

public class MegaChipEmulator extends SChipEmulator {

    @UnknownNullability
    private MegaChipProcessor<?> processor;

    @UnknownNullability
    private MegaChipMemory memory;

    @UnknownNullability
    private MegaChipDisplay<?> display;

    @UnknownNullability
    private MegaChipSoundSystem soundSystem;

    public MegaChipEmulator(Chip8EmulatorSettings emulatorSettings) {
        super(emulatorSettings, false);
    }

    @Override
    public @NotNull MegaChipProcessor<?> getProcessor() {
        return this.processor;
    }

    @Override
    public @NotNull MegaChipMemory getMemory() {
        return this.memory;
    }

    @Override
    public @NotNull MegaChipDisplay<?> getDisplay() {
        return this.display;
    }

    @Override
    public @NotNull MegaChipSoundSystem getSoundSystem() {
        return this.soundSystem;
    }

    @Override
    protected void initializeSoundSystem() {
        this.soundSystem = new MegaChipSoundSystem(this);
    }

    @Override
    protected void initializeDisplay() {
        this.display = new MegaChipDisplay<>(this);
    }

    @Override
    protected void initializeMemory() {
        this.memory = new MegaChipMemory(this);
    }

    @Override
    protected void initializeProcessor() {
        this.processor = new MegaChipProcessor<>(this);
    }


    @Override
    protected boolean waitFrameEnd(int flags) {
        if (!this.getProcessor().isMegaModeOn()) {
            return super.waitFrameEnd(flags);
        }
        return isSet(flags, Chip8Processor.CLS_EXECUTED);
    }

}
