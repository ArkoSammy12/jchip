package io.github.arkosammy12.jchip.sound;

import io.github.arkosammy12.jchip.Jchip;
import org.tinylog.Logger;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.SourceDataLine;

import static io.github.arkosammy12.jchip.sound.SoundSystem.SAMPLES_PER_FRAME;
import static io.github.arkosammy12.jchip.sound.SoundSystem.SAMPLE_RATE;

import java.io.Closeable;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Queue;

public final class DefaultAudioRenderer implements AudioRenderer, Closeable {

    private static final byte[] EMPTY_SAMPLES = new byte[SAMPLES_PER_FRAME * 2];
    private static final int BUFFER_SIZE = (SAMPLES_PER_FRAME * 2) * 5;

    private final SourceDataLine audioLine;
    private final FloatControl volumeControl;
    private final Queue<byte[]> samples = new LinkedList<>();
    private boolean paused = true;
    private boolean muted = false;
    private boolean started = false;

    public DefaultAudioRenderer(Jchip jchip) {
        try {
            AudioFormat format = new AudioFormat(SAMPLE_RATE, 16, 1, true, true);
            audioLine = AudioSystem.getSourceDataLine(format);
            audioLine.open(format, BUFFER_SIZE);
            FloatControl control = null;
            if (audioLine.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
                control = (FloatControl) audioLine.getControl(FloatControl.Type.MASTER_GAIN);
                control.setValue(20.0f * (float) Math.log10(50 / 100.0));
            }
            this.volumeControl = control;
            jchip.addFrameListener(_ -> this.onFrame());
        } catch (Exception e) {
            throw new RuntimeException("Failed to create Source Data Line for audio", e);
        }
    }

    public void setPaused(boolean paused) {
        this.paused = paused;
    }

    @Override
    public void setMuted(boolean muted) {
        this.muted = muted;
    }

    @Override
    public void pushSamples8(byte[] buf) {
        if (this.paused) {
            return;
        }
        if (buf.length != SAMPLES_PER_FRAME) {
            throw new IllegalArgumentException("Audio buffer sample size must be " + SAMPLES_PER_FRAME + "!");
        }
        byte[] buf16 = new byte[SAMPLES_PER_FRAME * 2];
        for (int i = 0; i < buf.length; i++) {
            int sample16 = buf[i] * 256;
            buf16[i * 2] = (byte) ((sample16 & 0xFF00) >>> 8);
            buf16[(i * 2) + 1] = (byte) (sample16 & 0xFF);
        }
        this.samples.offer(buf16);
    }

    @Override
    public void pushSamples16(short[] buf) {
        if (this.paused) {
            return;
        }
        if (buf.length != SAMPLES_PER_FRAME) {
            throw new IllegalArgumentException("Audio buffer sample size must be " + SAMPLES_PER_FRAME + "!");
        }
        byte[] buf16 = new byte[SAMPLES_PER_FRAME * 2];
        for (int i = 0; i < buf.length; i++) {
            int sample16 = buf[i];
            buf16[i * 2] = (byte) ((sample16 & 0xFF00) >>> 8);
            buf16[(i * 2) + 1] = (byte) (sample16 & 0xFF);
        }
        this.samples.offer(buf16);
    }

    public void setVolume(int volume) {
        if (this.volumeControl != null) {
            this.volumeControl.setValue(20.0f * (float) Math.log10(Math.clamp(volume, 0, 100) / 100.0));
        }
    }

    private void onFrame() {
        byte[] samples = this.samples.poll();
        if (samples == null || this.muted) {
            samples = EMPTY_SAMPLES;
        }

        if (!this.started) {
            this.audioLine.flush();
            this.audioLine.start();
            // Prefill with 0s
            samples = new byte [this.audioLine.getBufferSize()];
            this.started = true;
        }
        this.audioLine.write(samples, 0, samples.length);
    }

    public void close() {
        this.audioLine.stop();
        this.audioLine.flush();
        this.audioLine.close();
    }

}
