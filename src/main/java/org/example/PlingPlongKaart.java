package org.example;
import java.util.ArrayList;

public class PlingPlongKaart {
    private int x; //70
    private int y; //20
    private String[][] plingPlongMatrixLeeg = new String[70][20];
    private String[][] plingPlongMatrix;
    private int id;
    private int volgendeID = 1;
    private Gebruiker gebruiker;
    private String titel;
    private Audio audio;

    public PlingPlongKaart(Gebruiker gebruiker, String titel, Audio audio) {
        this.plingPlongMatrix = plingPlongMatrixLeeg;
        this.gebruiker = gebruiker;
        this.titel = titel;
        this.id = volgendeID++;
        this.x = 70;
        this.y = 20;
        this.audio = audio;
    }

    public String getAudioPad() {
        return audio != null ? audio.getPad() : null;
    }
    public Audio getAudio() {
        return audio;
    }
    public String getTitel() {
        return titel;
    }
    public String[][] getPlingPlongMatrix() {
        return plingPlongMatrix;
    }
    public int getId() {
        return id;
    }
    public String getGebruiker() {
        return gebruiker.naam;
    }
    public void setPlingPlongMatrix(String[][] plingPlongMatrix) {
        this.plingPlongMatrix = plingPlongMatrix;
    }
    public void setId(int id) {
        this.id = id;
    }

    public void nootToevoegen(int x, int y) {
        this.plingPlongMatrix[x][y] = "o";
    }

    public void nootVerwijderen(int x, int y) {
        this.plingPlongMatrix[x][y] = ".";
    }

    public void printKaart () {
        ArrayList<String> noten = new ArrayList<String>();
        noten.add("A"); noten.add("G"); noten.add("F"); noten.add("E"); noten.add("D"); noten.add("C"); noten.add("B");
        System.out.print("x  ");
        for (int i = 0; i < 70; i++) {
            System.out.print(i + " ");
            if (i < 10) {
                System.out.print(" ");
            }
        }
        System.out.println();
        for (int i = 0; i < y; i++) {
            System.out.print(noten.get(i%7) + "  ");
            for (int j = 0; j < x; j++)
                if (this.plingPlongMatrix[j][i] == null) {
                    System.out.print(".  ");
                } else {
                    System.out.print(this.plingPlongMatrix[j][i] + "  ");
                }
            System.out.println();
        }
    }

//    public void printInfo () {
//        System.out.println("Titel: " + this.getTitel()
//                + ", Gebruiker: " + gebruiker.getNaam()
//                + ", ID: " + this.getId() + ".");
//    }
}