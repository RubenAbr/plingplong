package org.example;

import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.xml.crypto.Data;
import java.io.IOException;
import java.sql.PreparedStatement;
import java.util.List;
import java.util.Scanner;

public class CLI {
    private Scanner scanner = new Scanner(System.in);
    private String huidigeGebruiker = null;

    public String getHuidigeGebruiker() {
        return huidigeGebruiker;
    }

    public void startUp() {
        DatabaseManager.initializeDatabase();
        while (huidigeGebruiker == null) {
            System.out.println("1. Inloggen");
            System.out.println("2. Registreren");
            System.out.print("Kies een optie [1 of 2]: ");
            String keuze = scanner.nextLine();

            if (keuze.equals("1")) {
                inloggen();
            } else if (keuze.equals("2")) {
                registreren();
            } else {
                System.out.println("Ongeldige keuze.");
            }
        }
        mainMenu();
    }

    private void inloggen() {
        System.out.print("Voer je naam in: ");
        String naam = scanner.nextLine();
        if (DatabaseManager.gebruikerBestaat(naam)) {
            huidigeGebruiker = naam;
            System.out.println("Ingelogd als " + huidigeGebruiker);
        } else {
            System.out.println("Gebruiker bestaat niet.");
        }
    }

    private void registreren() {
        System.out.print("Naam: ");
        String naam = scanner.nextLine();
        System.out.print("Emailadres: ");
        String email = scanner.nextLine();
        DatabaseManager.voegAccountToe(naam, email, "gebruiker");
        System.out.println("Registratie voltooid. Log nu in.");
    }

    private void mainMenu() {
        while (true) {
            System.out.println("\n--- Hoofdmenu ---");
            System.out.println("1. Kaart aanmaken");
            System.out.println("2. Kaart zoeken");
            System.out.println("3. Kaart verwijderen");
            System.out.println("4. Accountopties");
            System.out.println("5. Afsluiten");
            System.out.print("Keuze: ");
            String keuze = scanner.nextLine();

            switch (keuze) {
                case "1": kaartAanmaken(); break;
                case "2": kaartZoeken(); break;
                case "3": kaartVerwijderen(); break;
                case "4": accountOpties(); break;
                case "5": System.exit(0);
                default: System.out.println("Ongeldige keuze."); break;
            }
        }
    }

    private void kaartAanmaken() {
        System.out.print("Kaarttitel: ");
        String titel = scanner.nextLine();
        System.out.print("Pad naar audiobestand: ");
        String audioPad = scanner.nextLine();

        try {
            Audio audio = new Audio(audioPad);
            Gebruiker gebruiker = new Gebruiker(huidigeGebruiker, DatabaseManager.getEmail(huidigeGebruiker));
            PlingPlongKaart kaart = new PlingPlongKaart(gebruiker, titel, audio);

            while (true) {
                kaart.printKaart();
                System.out.print("Voer x,y in om een noot toe te voegen (of 'klaar'): ");
                String invoer = scanner.nextLine();
                if (invoer.equalsIgnoreCase("klaar")) break;

                try {
                    String[] coords = invoer.split(",");
                    int x = Integer.parseInt(coords[0]);
                    int y = Integer.parseInt(coords[1]);
                    kaart.nootToevoegen(x, y);
                } catch (Exception e) {
                    System.out.println("Ongeldige invoer.");
                }
            }
            DatabaseManager.voegKaartToe(kaart);
        } catch (Exception e) {
            System.out.println("Kon audio niet laden: " + e.getMessage());
        }
    }

    private void kaartVerwijderen() {
        List<Integer> ids = DatabaseManager.printAlleKaarten();
        System.out.print("Voer ID in van kaart om te verwijderen (of 'terug'): ");
        String keuze = scanner.nextLine();
        if (!keuze.equalsIgnoreCase("terug")) {
            try {
                int id = Integer.parseInt(keuze);
                if (ids.contains(id)) {
                    DatabaseManager.verwijderKaart(id);
                } else {
                    System.out.println("Ongeldige ID.");
                }
            } catch (NumberFormatException e) {
                System.out.println("Ongeldige invoer.");
            }
        }
    }

    private void kaartZoeken() {
        System.out.print("Zoekterm voor titel: ");
        String zoek = scanner.nextLine();
        List<PlingPlongKaart> resultaten = DatabaseManager.zoekKaarten(zoek);
        if (resultaten.isEmpty()) {
            System.out.println("Geen resultaten.");
            return;
        }

        for (int i = 0; i < resultaten.size(); i++) {
            System.out.println(i+1 + ": " + resultaten.get(i).getTitel());
        }

        System.out.print("Selecteer kaartnummer of typ 'terug': ");
        String keuze = scanner.nextLine().trim();
        if (keuze.equalsIgnoreCase("terug")) return;

        try {
            int index = Integer.parseInt(keuze)-1;
            if (index >= 0 && index < resultaten.size()) {
                toonKaartMenu(resultaten.get(index));
            }
        } catch (NumberFormatException e) {
            System.out.println("Ongeldige invoer.");
        }
    }

    private void toonKaartMenu(PlingPlongKaart kaart) {
        while (true) {
            kaart = DatabaseManager.haalKaartOp(kaart.getId());

            DatabaseManager.printInfoVanKaart(kaart.getId());
            kaart.printKaart();

            System.out.println("1. Speel audio");
            System.out.println("2. Verwijder kaart");
            System.out.println("3. Terug naar menu");
            System.out.print("Keuze: ");
            String keuze = scanner.nextLine();

            switch (keuze) {
                case "1":
                    try {
                        kaart.getAudio().herstart();
                    } catch (UnsupportedAudioFileException e) {
                        throw new RuntimeException(e);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    } catch (LineUnavailableException e) {
                        throw new RuntimeException(e);
                    }
                    toonAudioMenu(kaart);
                    break;
                case "2":
                    DatabaseManager.verwijderKaart(kaart.getId());
                    return;
                case "3":
                    return;
                default:
                    System.out.println("Ongeldige keuze.");
            }
        }
    }


    private void toonAudioMenu(PlingPlongKaart kaart) {
        while (true) {
            System.out.println("\n--- Audio Menu ---");
            System.out.println("1. Pauzeer");
            System.out.println("2. Hervat");
            System.out.println("3. Herstart");
            System.out.println("4. Spring naar tijd (in seconden)");
            System.out.println("5. Stop");
            System.out.println("6. Terug");
            System.out.print("Keuze: ");
            String audioKeuze = scanner.nextLine();

            try {
                switch (audioKeuze) {
                    case "1":
                        kaart.getAudio().pauzeer();
                        break;
                    case "2":
                        kaart.getAudio().hervat();
                        break;
                    case "3":
                        kaart.getAudio().herstart();
                        break;
                    case "4":
                        System.out.print("Voer tijd in seconden in: ");
                        String input = scanner.nextLine();
                        long seconden = Long.parseLong(input);
                        kaart.getAudio().springNaar(seconden * 1_000_000L);
                        break;
                    case "5":
                        kaart.getAudio().stop();
                        break;
                    case "6":
                        kaart.getAudio().stop();
                        return;
                    default:
                        System.out.println("Ongeldige keuze.");
                }
            } catch (Exception e) {
                System.out.println("Fout bij audio: " + e.getMessage());
            }
        }
    }


    private void accountOpties() {
        System.out.println("1. Account verwijderen");
        System.out.println("2. Terug");
        System.out.print("Keuze: ");
        String keuze = scanner.nextLine();
        if (keuze.equals("1")) {
            DatabaseManager.verwijderAccount(DatabaseManager.getEmail(huidigeGebruiker));
            huidigeGebruiker = null;
            startUp();
        }
    }
}
