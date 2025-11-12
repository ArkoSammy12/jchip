package io.github.arkosammy12.jchip.emulators;

import io.github.arkosammy12.jchip.cpu.HyperWaveChip64Processor;
import io.github.arkosammy12.jchip.config.EmulatorSettings;
import io.github.arkosammy12.jchip.video.HyperWaveChip64Display;

public class HyperWaveChip64Emulator extends XOChipEmulator {

    private HyperWaveChip64Processor<?> processor;
    private HyperWaveChip64Display<?> display;

    public HyperWaveChip64Emulator(EmulatorSettings emulatorSettings) {
        super(emulatorSettings);
    }

    @Override
    public HyperWaveChip64Processor<?> getProcessor() {
        return this.processor;
    }

    @Override
    public HyperWaveChip64Display<?> getDisplay() {
        return this.display;
    }

    @Override
    protected void initializeDisplay() {
        this.display = new HyperWaveChip64Display<>(this);
    }

    @Override
    protected void initializeProcessor() {
        this.processor = new HyperWaveChip64Processor<>(this);
    }


}
