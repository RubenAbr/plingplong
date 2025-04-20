package org.example;

public class Gebruiker implements Account {
    String naam;
    String emailadres;

    public Gebruiker (String naam, String emailadres) {
        this.naam = naam;
        this.emailadres = emailadres;
    }

    public String getNaam() {
        return naam;
    }
    public void setNaam(String naam) {
        this.naam = naam;
    }
    public String getEmailadres() {
        return emailadres;
    }
    public void setEmailadres(String emailadres) {
        this.emailadres = emailadres;
    }

    @Override
    public void toonInfo() {
        System.out.println("Naam: " + naam + ", Emailadres: " + emailadres + ".");
    }
}
