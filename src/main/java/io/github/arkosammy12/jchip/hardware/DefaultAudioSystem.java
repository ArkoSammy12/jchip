package io.github.arkosammy12.jchip.hardware;

import io.github.arkosammy12.jchip.base.AudioSystem;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.Clip;

public class DefaultAudioSystem implements AudioSystem {

    private Clip beepClip;
    private boolean isBeeping = false;

    @Override
    public void buzz() {
        if (!isBeeping) {
            try {
                AudioFormat af = new AudioFormat(44100f, 8, 1, true, false );
                beepClip = javax.sound.sampled.AudioSystem.getClip();
                byte[] data = new byte[44100];
                for (int i = 0; i < data.length; i++) {
                    data[i] = (byte)(Math.sin(2 * Math.PI * i / ((double) 44100 / 440)) * 127);
                }
                beepClip.open(af, data, 0, data.length);
                beepClip.loop(Clip.LOOP_CONTINUOUSLY);
                isBeeping = true;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void stopBuzz() {
        if (isBeeping && beepClip != null) {
            beepClip.stop();
            beepClip.close();
            isBeeping = false;
        }
    }
}
