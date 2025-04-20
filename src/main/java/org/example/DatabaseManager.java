package org.example;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DatabaseManager {
    private static final String DB_URL = "jdbc:sqlite:plingplong.db";

    public static void initializeDatabase() {
        try {
            Class.forName("org.sqlite.JDBC");
            try (Connection conn = DriverManager.getConnection(DB_URL)) {
                Statement stmt = conn.createStatement();
                String accounts = "CREATE TABLE IF NOT EXISTS accounts (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        "naam TEXT NOT NULL, " +
                        "email TEXT NOT NULL, " +
                        "type TEXT NOT NULL)";
                stmt.execute(accounts);

                String kaarten = "CREATE TABLE IF NOT EXISTS kaarten (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        "titel TEXT NOT NULL, " +
                        "gebruiker_email TEXT NOT NULL, " +
                        "matrix TEXT, " +
                        "audiopad TEXT)";
                stmt.execute(kaarten);

                System.out.println("Database is geïnitialiseerd.");
            } catch (SQLException e) {
                System.out.println("Fout bij database-initialisatie: " + e.getMessage());
            }
        } catch (ClassNotFoundException e) {
            System.out.println("SQLite JDBC Driver not found: " + e.getMessage());
        }

    }

    public static PlingPlongKaart haalKaartOp(int kaartId) {
        String sql = "SELECT * FROM kaarten WHERE id = ?";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, kaartId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                String titel = rs.getString("titel");
                String email = rs.getString("gebruiker_email");
                String naam = getNaamByEmail(email);
                String audioPad = rs.getString("audiopad");

                Audio audio = audioPad != null ? new Audio(audioPad) : null;
                Gebruiker gebruiker = new Gebruiker(naam, email);
                PlingPlongKaart kaart = new PlingPlongKaart(gebruiker, titel, audio);

                String matrixString = rs.getString("matrix");
                if (matrixString != null) {
                    kaart.setPlingPlongMatrix(matrixVanString(matrixString));
                }

                kaart.setId(kaartId);
                return kaart;
            }
        } catch (Exception e) {
            System.out.println("Fout bij ophalen kaart: " + e.getMessage());
        }
        return null;
    }

    public static void voegAccountToe(String naam, String email, String type) {
        String sql = "INSERT INTO accounts(naam, email, type) VALUES (?, ?, ?)";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, naam);
            pstmt.setString(2, email);
            pstmt.setString(3, type);
            pstmt.executeUpdate();
            System.out.println("Account toegevoegd.");
        } catch (SQLException e) {
            System.out.println("Fout bij toevoegen account: " + e.getMessage());
        }
    }

    public static void verwijderAccount(String email) {
        String sql = "DELETE FROM accounts WHERE email = ?";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, email);
            pstmt.executeUpdate();
            System.out.println("Account verwijderd.");
        } catch (SQLException e) {
            System.out.println("Fout bij verwijderen account: " + e.getMessage());
        }
    }

    public static void voegKaartToe(PlingPlongKaart kaart) {
        String sql = "INSERT INTO kaarten(titel, gebruiker_email, matrix, audiopad) VALUES (?, ?, ?, ?)";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, kaart.getTitel());
            pstmt.setString(2, kaart.getGebruiker());
            pstmt.setString(3, matrixNaarString(kaart.getPlingPlongMatrix()));
            pstmt.setString(4, kaart.getAudio() != null ? kaart.getAudioPad() : null);
            pstmt.executeUpdate();
            System.out.println("Kaart toegevoegd.");
        } catch (SQLException e) {
            System.out.println("Fout bij toevoegen kaart: " + e.getMessage());
        }
    }

    public static void verwijderKaart(int kaartId) {
        String sql = "DELETE FROM kaarten WHERE id = ?";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, kaartId);
            pstmt.executeUpdate();
            System.out.println("Kaart verwijderd.");
        } catch (SQLException e) {
            System.out.println("Fout bij verwijderen kaart: " + e.getMessage());
        }
    }

    public static boolean gebruikerBestaat(String naam) {
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement stmt = conn.prepareStatement("SELECT * FROM accounts WHERE naam = ?")) {
            stmt.setString(1, naam);
            ResultSet rs = stmt.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            return false;
        }
    }

    public static String getEmail(String naam) {
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement stmt = conn.prepareStatement("SELECT email FROM accounts WHERE naam = ?")) {
            stmt.setString(1, naam);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return rs.getString("email");
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static List<Integer> printAlleKaarten() {
        List<Integer> ids = new ArrayList<>();
        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement()) {
            ResultSet rs = stmt.executeQuery("SELECT * FROM kaarten");
            while (rs.next()) {
                int id = rs.getInt("id");
                String titel = rs.getString("titel");
                System.out.println("ID: " + id + " | Titel: " + titel);
                ids.add(id);
            }
        } catch (SQLException e) {
            System.out.println("Fout bij ophalen kaarten.");
        }
        return ids;
    }

    public static List<PlingPlongKaart> zoekKaarten(String zoekterm) {
        List<PlingPlongKaart> lijst = new ArrayList<>();
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement stmt = conn.prepareStatement("SELECT * FROM kaarten WHERE titel LIKE ?")) {
            stmt.setString(1, "%" + zoekterm + "%");
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                String titel = rs.getString("titel");
                String email = rs.getString("gebruiker_email");
                String naam = getNaamByEmail(email);
                String audioPad = rs.getString("audiopad");
                Audio audio = new Audio(audioPad);
                Gebruiker gebruiker = new Gebruiker(naam, email);
                PlingPlongKaart kaart = new PlingPlongKaart(gebruiker, titel, audio);
                lijst.add(kaart);
            }
        } catch (Exception e) {
            System.out.println("Fout bij zoeken kaarten: " + e.getMessage());
        }
        return lijst;
    }

    public static void printInfoVanKaart(int kaartId) {
        String sql = "SELECT k.titel, k.gebruiker_email, k.audiopad, k.matrix, a.naam " +
                "FROM kaarten k LEFT JOIN accounts a ON k.gebruiker_email = a.email " +
                "WHERE k.id = ?";

        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, kaartId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                String titel = rs.getString("titel");
                String naam = rs.getString("naam");

                System.out.println("----------------------");
                System.out.println("Kaarttitel: " + titel);
            } else {
                System.out.println("Geen kaart gevonden met ID " + kaartId);
            }

        } catch (SQLException e) {
            System.out.println("Fout bij ophalen kaartinfo: " + e.getMessage());
        }
    }


    public static String getNaamByEmail(String email) {
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement stmt = conn.prepareStatement("SELECT naam FROM accounts WHERE email = ?")) {
            stmt.setString(1, email);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return rs.getString("naam");
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }


    // Helper om matrix als string op te slaan
    private static String matrixNaarString(String[][] matrix) {
        StringBuilder sb = new StringBuilder();
        for (int y = 0; y < matrix[0].length; y++) {
            for (int x = 0; x < matrix.length; x++) {
                sb.append(matrix[x][y] == null ? "." : matrix[x][y]);
            }
            sb.append("\n");
        }
        return sb.toString();
    }

    private static String[][] matrixVanString(String matrixString) {
        String[] regels = matrixString.split("\n");
        int hoogte = regels.length;
        int breedte = regels[0].length();
        String[][] matrix = new String[breedte][hoogte];

        for (int y = 0; y < hoogte; y++) {
            for (int x = 0; x < breedte; x++) {
                char c = regels[y].charAt(x);
                matrix[x][y] = c == '.' ? null : String.valueOf(c);
            }
        }
        return matrix;
    }
}
