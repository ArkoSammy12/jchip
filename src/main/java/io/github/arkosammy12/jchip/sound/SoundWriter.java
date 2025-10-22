package io.github.arkosammy12.jchip.sound;

import org.tinylog.Logger;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.SourceDataLine;

import static io.github.arkosammy12.jchip.sound.SoundSystem.SAMPLES_PER_FRAME;
import static io.github.arkosammy12.jchip.sound.SoundSystem.SAMPLE_RATE;

import javax.sound.sampled.*;
import java.io.Closeable;

public final class SoundWriter implements Closeable {

    private static final byte[] EMPTY_SAMPLES = new byte[SAMPLES_PER_FRAME];

    private final SourceDataLine audioLine;
    private final FloatControl volumeControl;
    private int volume = 75;
    private boolean soundHasPlayed = false;

    public SoundWriter() {
        try {
            AudioFormat format = new AudioFormat(SAMPLE_RATE, 8, 1, true, true);
            audioLine = AudioSystem.getSourceDataLine(format);
            audioLine.open(format, SAMPLE_RATE);
            audioLine.start();

            FloatControl control = null;
            if (audioLine.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
                control = (FloatControl) audioLine.getControl(FloatControl.Type.MASTER_GAIN);
                control.setValue(20.0f * (float) Math.log10(volume / 100.0));
            }
            this.volumeControl = control;

        } catch (Exception e) {
            throw new RuntimeException("Error while initializing Source Data Line for audio: ", e);
        }
    }
    public void writeSamples(byte[] buf) {
        if (!this.audioLine.isRunning() && this.audioLine.isOpen()) {
            this.audioLine.start();
        }
        if (!this.soundHasPlayed) {
            if (this.audioLine.available() < this.audioLine.getBufferSize()) {
                this.audioLine.flush();
            }
        }
        this.audioLine.write(buf, 0, Math.min(buf.length, this.audioLine.available()));
        this.soundHasPlayed = true;
    }

    public void silence() {
        if (!this.audioLine.isRunning() && this.audioLine.isOpen()) {
            this.audioLine.start();
        }
        if (this.soundHasPlayed) {
            this.audioLine.write(EMPTY_SAMPLES, 0, Math.min(EMPTY_SAMPLES.length, this.audioLine.available()));
        }
    }

    public void volumeUp() {
        if (this.volumeControl != null) {
            this.volume = Math.clamp(this.volume + 25, 0, 100);
            this.volumeControl.setValue(20.0f * (float) Math.log10(volume / 100.0));
        }
    }

    public void volumeDown() {
        if (this.volumeControl != null) {
            this.volume = Math.clamp(this.volume - 25, 0, 100);
            this.volumeControl.setValue( 20.0f * (float) Math.log10(volume / 100.0));
        }
    }

    public void stop() {
        this.audioLine.stop();
        this.audioLine.flush();
        this.soundHasPlayed = false;
    }

    public void close() {
        this.stop();
        this.audioLine.close();
    }

}
