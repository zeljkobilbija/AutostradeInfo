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
            new Autoput("A3", "Napoli – Salerno", null),
            new Autoput("A4", "Torino – Trieste", null),
            new Autoput("A5", "Torino – Aosta – Monte Bianco", null),
            new Autoput("A6", "Torino – Savona", null),
            new Autoput("A7", "Milano – Genova", null),
            new Autoput("A8", "Milano – Varese", null),
            new Autoput("A9", "Lainate – Como – Chiasso", null),
            new Autoput("A10", "Genova – Ventimiglia", null),
            new Autoput("A11", "Firenze – Pisa", null),
            new Autoput("A12", "Genova – Rosignano · Roma – Civitavecchia", null),
            new Autoput("A13", "Bologna – Padova", null),
            new Autoput("A14", "Bologna – Taranto", null),
            new Autoput("A15", "Parma – La Spezia", null),
            new Autoput("A16", "Napoli – Canosa", null),
            new Autoput("A18", "Messina – Catania · Siracusa – Gela", null),
            new Autoput("A19", "Palermo – Catania", null),
            new Autoput("A20", "Messina – Palermo", null),
            new Autoput("A21", "Torino – Piacenza – Brescia", null),
            new Autoput("A22", "Brennero – Modena", null),
            new Autoput("A23", "Palmanova – Tarvisio", null),
            new Autoput("A24", "Roma – L'Aquila – Teramo", null),
            new Autoput("A25", "Torano – Pescara", null),
            new Autoput("A26", "Genova Voltri – Gravellona Toce", null),
            new Autoput("A27", "Venezia – Belluno", null),
            new Autoput("A30", "Caserta – Salerno", null)
    );

    public static Autoput poId(String id) {
        for (Autoput a : SVE) if (a.id.equals(id)) return a;
        return null;
    }
}
