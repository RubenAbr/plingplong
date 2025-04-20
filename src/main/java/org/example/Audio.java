package org.example;

import javax.sound.sampled.*;
import java.io.File;
import java.io.IOException;

public class Audio {
    Long huidigeFrame;
    Clip clip;
    String status;

    AudioInputStream audioInputStream;
    static String bestandPad;

    public Audio(String bestandPad)
            throws UnsupportedAudioFileException, IOException, LineUnavailableException {
        Audio.bestandPad = bestandPad;

        audioInputStream = AudioSystem.getAudioInputStream(new File(bestandPad).getAbsoluteFile());
        clip = AudioSystem.getClip();
        clip.open(audioInputStream);

        clip.addLineListener(event -> {
            if (event.getType() == LineEvent.Type.STOP) {
                if (clip.getMicrosecondPosition() >= clip.getMicrosecondLength()) {
                    System.out.println("Audio is afgelopen.");
                    status = "gestopt";
                }
            }
        });
//        clip.loop(Clip.LOOP_CONTINUOUSLY);
    }

    public String getPad() {
        return bestandPad;
    }

    public void speel() {
        clip.start();
        status = "afspelen";
        System.out.println("Afspelen gestart.");
    }

    public void pauzeer() {
        if ("gepauzeerd".equals(status)) {
            System.out.println("Audio is al gepauzeerd.");
            return;
        }
        huidigeFrame = clip.getMicrosecondPosition();
        clip.stop();
        status = "gepauzeerd";
        System.out.println("Audio gepauzeerd.");
    }

    public void hervat() throws UnsupportedAudioFileException, IOException, LineUnavailableException {
        if ("afspelen".equals(status)) {
            System.out.println("Audio speelt al af.");
            return;
        }
        clip.close();
        resetAudioStream();
        clip.setMicrosecondPosition(huidigeFrame);
        speel();
        System.out.println("Audio hervat.");
    }

    public void herstart() throws UnsupportedAudioFileException, IOException, LineUnavailableException {
        clip.stop();
        clip.close();
        resetAudioStream();
        huidigeFrame = 0L;
        clip.setMicrosecondPosition(0);
        speel();
        System.out.println("Audio herstart.");
    }

    public void stop() throws UnsupportedAudioFileException, IOException, LineUnavailableException {
        huidigeFrame = 0L;
        clip.stop();
        clip.close();
        status = "gestopt";
        System.out.println("Audio gestopt.");
    }

    public void springNaar(long microseconden) throws UnsupportedAudioFileException, IOException, LineUnavailableException {
        if (microseconden > 0 && microseconden < clip.getMicrosecondLength()) {
            clip.stop();
            clip.close();
            resetAudioStream();
            huidigeFrame = microseconden;
            clip.setMicrosecondPosition(microseconden);
            speel();
            System.out.println("Gesprongen naar " + microseconden + " microseconden.");
        } else {
            System.out.println("Ongeldige tijd opgegeven.");
        }
    }

    public void resetAudioStream() throws UnsupportedAudioFileException, IOException, LineUnavailableException {
        audioInputStream = AudioSystem.getAudioInputStream(new File(bestandPad).getAbsoluteFile());
        clip.open(audioInputStream);
        //clip.loop(Clip.LOOP_CONTINUOUSLY);
    }
}
