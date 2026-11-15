package com.zeljkobilbija001.autostradeinfo;

import java.util.Arrays;
import java.util.List;

/**
 * Spisak italijanskih autostrada. Novi autoput: dodaj JSON u assets/autoputevi
 * (node alati/autostrade_skripta.js A1) i upiši ime datoteke umesto null.
 */
public final class Autoputevi {

    private Autoputevi() { }

    public static final List<Autoput> SVE = Arrays.asList(
            new Autoput("A1", "Milano – Napoli", "autoputevi/a1.json"),
            new Autoput("A2", "Salerno – Reggio Calabria", "autoputevi/a2.json"),
            new Autoput("A3", "Napoli – Salerno", "autoputevi/a3.json"),
            new Autoput("A4", "Torino – Trieste", "autoputevi/a4.json"),
            new Autoput("A5", "Torino – Aosta – Monte Bianco", "autoputevi/a5.json"),
            new Autoput("A6", "Torino – Savona", "autoputevi/a6.json"),
            new Autoput("A7", "Milano – Genova", "autoputevi/a7.json"),
            new Autoput("A8", "Milano – Varese", "autoputevi/a8.json"),
            new Autoput("A9", "Lainate – Como – Chiasso", "autoputevi/a9.json"),
            new Autoput("A10", "Genova – Ventimiglia", "autoputevi/a10.json"),
            new Autoput("A11", "Firenze – Pisa", "autoputevi/a11.json"),
            new Autoput("A12", "Genova – Rosignano · Roma – Civitavecchia", "autoputevi/a12.json"),
            new Autoput("A13", "Bologna – Padova", "autoputevi/a13.json"),
            new Autoput("A14", "Bologna – Taranto", "autoputevi/a14.json"),
            new Autoput("A15", "Parma – La Spezia", "autoputevi/a15.json"),
            new Autoput("A16", "Napoli – Canosa", "autoputevi/a16.json"),
            new Autoput("A18", "Messina – Catania · Siracusa – Gela", "autoputevi/a18.json"),
            new Autoput("A19", "Palermo – Catania", "autoputevi/a19.json"),
            new Autoput("A20", "Messina – Palermo", "autoputevi/a20.json"),
            new Autoput("A21", "Torino – Piacenza – Brescia", "autoputevi/a21.json"),
            new Autoput("A22", "Brennero – Modena", "autoputevi/a22.json"),
            new Autoput("A23", "Palmanova – Tarvisio", "autoputevi/a23.json"),
            new Autoput("A24", "Roma – L'Aquila – Teramo", "autoputevi/a24.json"),
            new Autoput("A25", "Torano – Pescara", "autoputevi/a25.json"),
            new Autoput("A26", "Genova Voltri – Gravellona Toce", "autoputevi/a26.json"),
            new Autoput("A27", "Venezia – Belluno", "autoputevi/a27.json"),
            new Autoput("A28", "Portogruaro – Conegliano", "autoputevi/a28.json"),
            new Autoput("A29", "Palermo – Mazara del Vallo", "autoputevi/a29.json"),
            new Autoput("A30", "Caserta – Salerno", "autoputevi/a30.json"),
            new Autoput("A31", "Rovigo – Vicenza – Piovene Rocchette", "autoputevi/a31.json"),
            new Autoput("A32", "Torino – Bardonecchia", "autoputevi/a32.json"),
            new Autoput("A33", "Asti – Cuneo", "autoputevi/a33.json"),
            new Autoput("A34", "Villesse – Gorizia", "autoputevi/a34.json"),
            new Autoput("A35", "Brescia – Milano (BreBeMi)", "autoputevi/a35.json"),
            new Autoput("A36", "Pedemontana Lombarda", "autoputevi/a36.json"),
            new Autoput("A50", "Tangenziale Ovest di Milano", "autoputevi/a50.json"),
            new Autoput("A51", "Tangenziale Est di Milano", "autoputevi/a51.json"),
            new Autoput("A52", "Tangenziale Nord di Milano", "autoputevi/a52.json"),
            new Autoput("A53", "Bereguardo – Pavia", "autoputevi/a53.json"),
            new Autoput("A54", "Tangenziale di Pavia", "autoputevi/a54.json"),
            new Autoput("A55", "Tangenziale di Torino", "autoputevi/a55.json"),
            new Autoput("A56", "Tangenziale di Napoli", "autoputevi/a56.json"),
            new Autoput("A57", "Tangenziale di Mestre", "autoputevi/a57.json"),
            new Autoput("A58", "Tangenziale Est Esterna di Milano", "autoputevi/a58.json"),
            new Autoput("A60", "Tangenziale di Varese", "autoputevi/a60.json"),
            new Autoput("A90", "Grande Raccordo Anulare (Roma)", "autoputevi/a90.json"),
            new Autoput("A91", "Roma – Fiumicino", "autoputevi/a91.json")
    );

    public static Autoput poId(String id) {
        for (Autoput a : SVE) if (a.id.equals(id)) return a;
        return null;
    }
}
