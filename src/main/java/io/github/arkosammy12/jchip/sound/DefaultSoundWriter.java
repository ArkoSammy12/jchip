package io.github.arkosammy12.jchip.sound;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.SourceDataLine;

import static io.github.arkosammy12.jchip.sound.SoundSystem.SAMPLES_PER_FRAME;
import static io.github.arkosammy12.jchip.sound.SoundSystem.SAMPLE_RATE;

import java.io.Closeable;
import java.util.LinkedList;
import java.util.Queue;

public final class DefaultSoundWriter implements SoundWriter, Closeable {

    private static final byte[] EMPTY_SAMPLES = new byte[SAMPLES_PER_FRAME * 2];

    private final SourceDataLine audioLine;
    private final FloatControl volumeControl;
    private final Queue<byte[]> samples = new LinkedList<>();
    private boolean paused = true;

    public DefaultSoundWriter() {
        try {
            AudioFormat format = new AudioFormat(SAMPLE_RATE, 16, 1, true, true);
            audioLine = AudioSystem.getSourceDataLine(format);
            audioLine.open(format);
            FloatControl control = null;
            if (audioLine.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
                control = (FloatControl) audioLine.getControl(FloatControl.Type.MASTER_GAIN);
                control.setValue(20.0f * (float) Math.log10(50 / 100.0));
            }
            audioLine.flush();
            audioLine.start();
            this.volumeControl = control;

        } catch (Exception e) {
            throw new RuntimeException("Error while initializing Source Data Line for audio: ", e);
        }
    }

    public void setPaused(boolean paused) {
        this.paused = paused;
    }

    @Override
    public void pushSamples(byte[] buf) {
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

    public void setVolume(int volume) {
        if (this.volumeControl != null) {
            this.volumeControl.setValue(20.0f * (float) Math.log10(Math.clamp(volume, 0, 100) / 100.0));
        }
    }

    /*

    @Override
    public void volumeUp() {
        if (this.volumeControl != null) {
            this.volume = Math.clamp(this.volume + 25, 0, 100);
            this.volumeControl.setValue(20.0f * (float) Math.log10(volume / 100.0));
        }
    }

    @Override
    public void volumeDown() {
        if (this.volumeControl != null) {
            this.volume = Math.clamp(this.volume - 25, 0, 100);
            this.volumeControl.setValue(20.0f * (float) Math.log10(volume / 100.0));
        }
    }

     */

    public void onFrame() {
        byte[] samples = this.samples.poll();
        if (samples == null) {
            samples = EMPTY_SAMPLES;
        }
        this.audioLine.write(samples, 0, Math.min(this.getBytesToWrite(samples.length), this.audioLine.available()));
    }

    public void close() {
        this.audioLine.stop();
        this.audioLine.flush();
        this.audioLine.close();
    }

    private int getBytesToWrite(int idealLength) {
        int diff = (this.audioLine.getBufferSize() - this.audioLine.available()) - (SAMPLES_PER_FRAME * 2);
        if (diff < 0) {
            return idealLength;
        }
        return Math.max(idealLength - diff, 0);
    }

}
