package org.example;

public class Administrator implements Account{
    String naam;
    String emailadres;

    public Administrator (String naam, String emailadres) {
        this.naam = naam;
        this.emailadres = emailadres;
    }

    @Override
    public void toonInfo() {
        System.out.println("Naam: " + naam + ", Emailadres: " + emailadres + ". Is een administrator");
    }
}
