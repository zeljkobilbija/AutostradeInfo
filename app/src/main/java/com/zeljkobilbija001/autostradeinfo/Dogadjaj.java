package com.zeljkobilbija001.autostradeinfo;

import java.text.Normalizer;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Jedna saobraćajna poruka iz RSS-a CCISS-a (www.cciss.it, Ministero delle Infrastrutture e dei Trasporti).
 * Primer opisa: "incidente dalle 13:14 del 19 novembre 2026 tra Svincolo Altedo e Svincolo Ferrara Sud
 * in direzione Padova". Koordinata nema: mesto se određuje po nazivima izlaza iz podataka aplikacije.
 */
public final class Dogadjaj {

    /** Vrste, poređane po važnosti (manji broj = važnije). */
    public static final int ZATVORENO = 0, UDES = 1, OPASNOST = 2, KOLONA = 3,
            TRAKA = 4, SPORO = 5, PRILAZ = 6, OSTALO = 7;

    public final String id;
    public final int vrsta;
    public final String deonica;   // "Altedo – Ferrara Sud" (može biti prazno)
    public final String smer;      // "→ Padova"
    public final String opis;      // italijanski tekst CCISS-a
    public final int kasnjenjeMin; // CCISS ga ne daje: uvek 0
    public final long pocetak;     // ms, 0 = nepoznato
    public final double lat, lon;  // prvo mesto; NaN ako se mesto ne nađe
    public final double lat2, lon2; // drugo mesto (određuje smer); NaN ako nema

    private Dogadjaj(String id, int vrsta, String deonica, String smer, String opis, long pocetak,
                     double lat, double lon, double lat2, double lon2) {
        this.id = id;
        this.vrsta = vrsta;
        this.deonica = deonica;
        this.smer = smer;
        this.opis = opis;
        this.kasnjenjeMin = 0;
        this.pocetak = pocetak;
        this.lat = lat;
        this.lon = lon;
        this.lat2 = lat2;
        this.lon2 = lon2;
    }

    /** Da li zbog ove poruke vredi poslati obaveštenje (udes, kolona, zatvoren put, opasnost). */
    public boolean zaObavestenje() {
        // vozila u kvaru su u Italiji stalno (desetine dnevno): samo u listi, bez obaveštenja
        if (vrsta == OPASNOST && (opis.contains("avaria") || opis.contains("veicolo fermo"))) return false;
        return vrsta == ZATVORENO || vrsta == UDES || vrsta == OPASNOST || vrsta == KOLONA;
    }

    public boolean imaKoordinate() {
        return !Double.isNaN(lat) && !Double.isNaN(lon);
    }

    // ---------- Čitanje jedne stavke RSS-a ----------

    private static final String MESTO = "(?:[\\d,.]+ (?:km|m) (?:prima di|dopo) )?"
            + "(?:Svincolo|Allacciamento|Barriera|Area di servizio|Area di parcheggio|Parcheggio|Uscita|Stazione"
            + "|Interconnessione|Diramazione|Galleria|Confine|Inizio|Fine|Incrocio)\\b";
    private static final Pattern VREME = Pattern.compile(
            "dalle (\\d{1,2}):(\\d{2})(?: alle \\d{1,2}:\\d{2})? del (\\d{1,2}) ([a-z]+) (\\d{4})");
    private static final Pattern TRA = Pattern.compile(" tra (" + MESTO + ".*?) e (" + MESTO + ".*?)"
            + "(?: in direzione (.+))?$");
    private static final Pattern A = Pattern.compile(" a (" + MESTO + ".*?)(?: in direzione (.+))?$");
    private static final Pattern POMAK = Pattern.compile("^([\\d,.]+) (km|m) (prima di|dopo) (.*)$");
    private static final List<String> MESECI = Arrays.asList("gennaio", "febbraio", "marzo", "aprile", "maggio",
            "giugno", "luglio", "agosto", "settembre", "ottobre", "novembre", "dicembre");

    /**
     * Poruka za autostradu sa podacima {@code p} (može biti null: tada bez mesta).
     * {@code naslov} = "A13 Bologna-Padova", {@code tekst} = opis bez "(Fonte: …)".
     */
    static Dogadjaj izRss(String naslov, String tekst, long objavljeno, Podaci p) {
        String t = tekst.replaceAll("\\s+", " ").trim();
        if (t.startsWith(naslov)) t = t.substring(naslov.length()).trim();
        String opis = t;

        long pocetak = objavljeno;
        Matcher mv = VREME.matcher(t);
        if (mv.find()) {
            int mesec = MESECI.indexOf(mv.group(4));
            if (mesec >= 0) {
                Calendar k = Calendar.getInstance(TimeZone.getTimeZone("Europe/Rome"), Locale.ITALY);
                k.clear();
                k.set(Integer.parseInt(mv.group(5)), mesec, Integer.parseInt(mv.group(3)),
                        Integer.parseInt(mv.group(1)), Integer.parseInt(mv.group(2)));
                pocetak = k.getTimeInMillis();
            }
            t = (t.substring(0, mv.start()) + t.substring(mv.end())).replaceAll("\\s+", " ").trim();
        }

        String mesto1 = null, mesto2 = null, pravac = "";
        String vrstaTekst = t;
        Matcher mt = TRA.matcher(" " + t);
        if (mt.find()) {
            mesto1 = mt.group(1).trim();
            mesto2 = mt.group(2).trim();
            pravac = mt.group(3) != null ? mt.group(3).trim() : "";
            vrstaTekst = (" " + t).substring(0, mt.start()).trim();
        } else {
            Matcher ma = A.matcher(" " + t);
            if (ma.find()) {
                mesto1 = ma.group(1).trim();
                pravac = ma.group(2) != null ? ma.group(2).trim() : "";
                vrstaTekst = (" " + t).substring(0, ma.start()).trim();
            }
        }

        int vrsta = vrsta(vrstaTekst.toLowerCase(Locale.ITALIAN));
        String deonica = mesto1 == null ? "" : bezPomaka(mesto1) + (mesto2 != null ? " – " + bezPomaka(mesto2) : "");
        if (pravac.startsWith("da ")) pravac = pravac.substring(3);
        String smer = pravac.isEmpty() ? "" : "→ " + pravac;

        double[] t1 = p != null && mesto1 != null ? tacka(p, mesto1, mesto2, true) : null;
        double[] t2 = p != null && mesto2 != null ? tacka(p, mesto2, mesto1, false) : null;
        if (t1 == null && t2 != null) { // npr. "tra Allacciamento A1 … e Svincolo Candela": bar drugo mesto
            t1 = t2;
            t2 = null;
        }
        // samo jedno mesto ("a Svincolo Reggio Emilia in direzione Milano"): smer iz "in direzione",
        // upoređen sa krajevima autostrade; druga tačka 1 km dalje u tom smeru
        if (t1 != null && t2 == null && !pravac.isEmpty()) {
            Stavka s = nadji(p, samoNaziv(mesto1 != null ? mesto1 : mesto2));
            if (s != null && s.deonica >= 0 && s.deonica < p.deonice.size()) {
                Deonica d = p.deonice.get(s.deonica);
                Set<String> r = reci(pravac);
                boolean plus = !java.util.Collections.disjoint(r, reci(d.plus));
                boolean minus = !java.util.Collections.disjoint(r, reci(d.minus));
                if (plus != minus) t2 = naKm(p, s.deonica, s.km + (plus ? 1 : -1));
            }
        }

        String id = naslov + "|" + vrstaTekst + "|" + deonica + "|" + smer + "|" + pocetak;
        return new Dogadjaj(Integer.toHexString(id.hashCode()), vrsta, deonica, smer, opis, pocetak,
                t1 != null ? t1[0] : Double.NaN, t1 != null ? t1[1] : Double.NaN,
                t2 != null ? t2[0] : Double.NaN, t2 != null ? t2[1] : Double.NaN);
    }

    static int vrsta(String t) {
        if (t.contains("contromano")) return OPASNOST;
        if (t.startsWith("ripristino")) return SPORO;
        // radnici na putu (i kad uklanjaju ostatke udesa ili teret): radovi, ne nova opasnost
        if (t.startsWith("personale su strada")) return TRAKA;
        if (t.contains("chius") && (t.contains("uscita") || t.contains("entrata") || t.contains("svincolo")
                || t.contains("ingresso") || t.contains("allacciamento"))) return PRILAZ;
        if (t.contains("chiusa") || t.contains("chiuso") || t.contains("chiusura") || t.contains("bloccat")
                || t.contains("interrott")) return ZATVORENO;
        if (t.startsWith("code") || t.startsWith("coda")) return KOLONA;
        if (t.startsWith("incidente")) return UDES;
        if (t.startsWith("traffico rallentato") && t.contains("incidente")) return KOLONA;
        if (t.startsWith("traffico rallentato") || t.startsWith("veicoli lenti")) return SPORO; // "… causa perdita di carico"
        if (t.contains("avaria") || t.contains("veicolo fermo") || t.contains("perdita di carico")
                || t.contains("ostacolo") || t.contains("pedoni") || t.contains("animali") || t.contains("incendio")
                || t.contains("nebbia") || t.contains("neve") || t.contains("ghiaccio") || t.contains("grandine")
                || t.contains("vento forte") || t.contains("allagament") || t.contains("frana")
                || t.contains("visibilità")) return OPASNOST;
        if (t.contains("lavori") || t.contains("cantiere") || t.contains("restringimento")
                || t.contains("corsia") || t.contains("personale su strada")) return TRAKA;
        if (t.contains("rallentat") || t.contains("veicoli lenti") || t.contains("traffico intenso")
                || t.contains("scortati") || t.contains("pioggia")) return SPORO;
        return OSTALO;
    }

    // ---------- Mesto po nazivu ----------

    private static String bezPomaka(String m) {
        Matcher x = POMAK.matcher(m);
        String s = x.matches() ? x.group(4) : m;
        return s.replaceFirst("^(Svincolo|Allacciamento|Barriera|Uscita|Stazione) ", "")
                .replaceFirst("^Di ", "");
    }

    private static String samoNaziv(String m) {
        Matcher x = POMAK.matcher(m);
        return x.matches() ? x.group(4) : m;
    }

    /**
     * [lat, lon] mesta (izlaza ili odmorišta) iz podataka; uz "N km prima di/dopo" pomera se duž
     * autostrade u smeru od prvog ka drugom mestu. null ako se naziv ne prepozna.
     */
    private static double[] tacka(Podaci p, String mesto, String drugo, boolean prvo) {
        double pomak = 0;
        Matcher x = POMAK.matcher(mesto);
        if (x.matches()) {
            try {
                pomak = Double.parseDouble(x.group(1).replace(".", "").replace(',', '.'));
            } catch (NumberFormatException e) {
                pomak = 0;
            }
            if ("m".equals(x.group(2))) pomak /= 1000;
            if ("prima di".equals(x.group(3))) pomak = -pomak;
        }
        Stavka s = nadji(p, samoNaziv(mesto));
        if (s == null) return null;
        if (Math.abs(pomak) < 0.3) return new double[]{s.lat, s.lon};
        // smer vožnje: od prvog mesta ka drugom (ako se drugo ne zna, pomak se ne primenjuje)
        Stavka s2 = drugo == null ? null : nadji(p, samoNaziv(drugo));
        if (s2 == null || s2.deonica != s.deonica || Math.abs(s2.km - s.km) < 0.05) return new double[]{s.lat, s.lon};
        double znak = (prvo ? s2.km > s.km : s.km > s2.km) ? 1 : -1;
        return naKm(p, s.deonica, s.km + znak * pomak);
    }

    /** Tačka na autostradi na datom km (linearno između susednih mesta sa koordinatama). */
    private static double[] naKm(Podaci p, int deonica, double km) {
        Stavka pre = null, posle = null;
        for (Stavka s : p.stavke) {
            if (s.deonica != deonica || Double.isNaN(s.lat)) continue;
            if (s.km <= km && (pre == null || s.km > pre.km)) pre = s;
            if (s.km >= km && (posle == null || s.km < posle.km)) posle = s;
        }
        if (pre == null && posle == null) return null;
        if (pre == null) return new double[]{posle.lat, posle.lon};
        if (posle == null || posle.km - pre.km < 0.01) return new double[]{pre.lat, pre.lon};
        double u = (km - pre.km) / (posle.km - pre.km);
        return new double[]{pre.lat + u * (posle.lat - pre.lat), pre.lon + u * (posle.lon - pre.lon)};
    }

    private static final Set<String> STOP = new HashSet<>(Arrays.asList("di", "del", "della", "dei", "delle",
            "d", "e", "a", "al", "in", "san", "santa", "sant", "area", "servizio", "parcheggio", "svincolo",
            "uscita", "casello", "barriera", "allacciamento", "raccordo", "interconnessione", "diramazione",
            "tangenziale", "autostrada", "via", "km", "sp", "ss", "sr", "stazione"));
    private static final List<String> STRANE = Arrays.asList("nord", "sud", "centro", "est", "ovest");

    /** Mesto iz podataka čiji naziv najviše liči (isto pravilo kao alati/km_izlazi.js). */
    static Stavka nadji(Podaci p, String naziv) {
        Set<String> a = reci(naziv);
        if (a.isEmpty()) return null;
        boolean odmoriste = naziv.startsWith("Area") || naziv.startsWith("Parcheggio");
        // "Allacciamento A1 Milano-Napoli" je petlja sa drugim autoputem: samo među petljama
        // (inače "Napoli" iz naziva petlje kod Milana pogodi izlaz kod Napulja)
        boolean petlja = naziv.startsWith("Allacciamento") || naziv.startsWith("Interconnessione")
                || naziv.startsWith("Diramazione");
        Stavka naj = null;
        double najS = 0;
        for (Stavka s : p.stavke) {
            if (s.naziv.isEmpty() || Double.isNaN(s.lat)) continue;
            if (petlja && s.tip != Stavka.CVORISTE) continue;
            double sl = slicnost(a, reci(s.naziv));
            if (sl <= 0) continue;
            if (odmoriste == (s.tip != Stavka.IZLAZ && s.tip != Stavka.CVORISTE)) sl += 0.05;
            if (sl > najS) {
                najS = sl;
                naj = s;
            }
        }
        return najS >= 0.6 ? naj : null;
    }

    private static Set<String> reci(String s) {
        String n = Normalizer.normalize(s, Normalizer.Form.NFD).replaceAll("\\p{M}", "").toLowerCase(Locale.ROOT);
        Set<String> r = new HashSet<>();
        for (String w : n.split("[^a-z0-9]+")) {
            if (w.length() >= 2 && !STOP.contains(w) && !w.matches("a\\d+")) r.add(w);
        }
        return r;
    }

    private static double slicnost(Set<String> a, Set<String> b) {
        if (b.isEmpty()) return 0;
        int z = 0;
        for (String w : a) if (b.contains(w)) z++;
        StringBuilder sa = new StringBuilder(), sb = new StringBuilder();
        for (String w : STRANE) {
            if (a.contains(w)) sa.append(w);
            if (b.contains(w)) sb.append(w);
        }
        if (sa.length() > 0 && sb.length() > 0 && !sa.toString().equals(sb.toString())) return 0;
        if ((sa.length() > 0) != (sb.length() > 0) && z <= 1 && Math.min(a.size(), b.size()) <= 1) return 0.5;
        return (double) z / Math.min(a.size(), b.size()) - 0.01 * Math.abs(a.size() - b.size());
    }
}
