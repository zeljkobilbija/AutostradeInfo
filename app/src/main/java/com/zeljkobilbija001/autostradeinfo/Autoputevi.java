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
            new Autoput("A30", "Caserta – Salerno", "autoputevi/a30.json")
    );

    public static Autoput poId(String id) {
        for (Autoput a : SVE) if (a.id.equals(id)) return a;
        return null;
    }
}
