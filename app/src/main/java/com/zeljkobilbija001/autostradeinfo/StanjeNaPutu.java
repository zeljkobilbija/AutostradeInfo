package com.zeljkobilbija001.autostradeinfo;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Stanje na putu za jednu autostradu: poruke CCISS-a (Viaggiare Informati, Ministero delle
 * Infrastrutture e dei Trasporti) iz javnog RSS-a www.cciss.it/rss (bez ključa, sve Italije odjednom).
 * RSS se čuva 60 s, pa provere više autostrada ne preuzimaju isto ponovo.
 */
public final class StanjeNaPutu {

    public interface Odgovor {
        /** Poziva se na glavnoj niti; null ako podaci nisu stigli. */
        void stiglo(List<Dogadjaj> dogadjaji);
    }

    private static final String RSS = "https://www.cciss.it/rss";
    private static final long KES_MS = 60_000L;
    private static final ExecutorService NIT = Executors.newSingleThreadExecutor();
    private static final Pattern STAVKA = Pattern.compile("<item>([\\s\\S]*?)</item>");
    /** Naslov počinje oznakom autostrade: "A13 Bologna-Padova", "A1 Diramazione Roma Nord". */
    private static final Pattern OZNAKA = Pattern.compile("^(A\\d+)\\b");

    private static String kesTekst;
    private static long kesVreme;

    private StanjeNaPutu() { }

    public static void ucitaj(Context c, String autoput, Odgovor odgovor) {
        Context app = c.getApplicationContext();
        Handler glavna = new Handler(Looper.getMainLooper());
        NIT.execute(() -> {
            List<Dogadjaj> l;
            try {
                l = preuzmi(app, autoput);
            } catch (Exception e) {
                l = null;
            }
            List<Dogadjaj> konacno = l;
            glavna.post(() -> odgovor.stiglo(konacno));
        });
    }

    /**
     * Preuzimanje na tekućoj niti (i za proveru u pozadini). Najnovija poruka ide prva bez obzira
     * na važnost (važnost pokazuje boja), poruke bez vremena na kraj; isto vreme: važnija prva.
     */
    public static List<Dogadjaj> preuzmi(Context c, String autoput) throws Exception {
        String xml = rss();
        Podaci p = Podaci.za(c, Autoputevi.poId(autoput));
        List<Dogadjaj> l = new ArrayList<>();
        Matcher m = STAVKA.matcher(xml);
        while (m.find()) {
            String s = m.group(1);
            String naslov = tekst(s, "title");
            Matcher o = OZNAKA.matcher(naslov);
            if (!o.find() || !autoput.equalsIgnoreCase(o.group(1))) continue;
            String opis = tekst(s, "description");
            int fonte = opis.indexOf("<br");
            if (fonte >= 0) opis = opis.substring(0, fonte);
            l.add(Dogadjaj.izRss(naslov, opis.trim(), vreme(tekst(s, "pubDate")), p));
        }
        Collections.sort(l, (a, b) -> a.pocetak != b.pocetak ? Long.compare(b.pocetak, a.pocetak)
                : Integer.compare(a.vrsta, b.vrsta));
        return l;
    }

    private static synchronized String rss() throws Exception {
        long sad = System.currentTimeMillis();
        if (kesTekst != null && sad - kesVreme < KES_MS) return kesTekst;
        kesTekst = get(RSS);
        kesVreme = sad;
        return kesTekst;
    }

    private static String tekst(String s, String tag) {
        int a = s.indexOf("<" + tag + ">"), b = s.indexOf("</" + tag + ">");
        if (a < 0 || b < a) return "";
        String t = s.substring(a + tag.length() + 2, b).trim();
        if (t.startsWith("<![CDATA[")) t = t.substring(9, t.length() - 3);
        return t.replace("&lt;", "<").replace("&gt;", ">").replace("&quot;", "\"")
                .replace("&#39;", "'").replace("&apos;", "'").replace("&amp;", "&");
    }

    private static long vreme(String rfc) {
        try {
            Date d = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz", Locale.US).parse(rfc.trim());
            return d == null ? 0 : d.getTime();
        } catch (Exception e) {
            return 0;
        }
    }

    private static String get(String adresa) throws Exception {
        HttpURLConnection v = (HttpURLConnection) new URL(adresa).openConnection();
        v.setConnectTimeout(10000);
        v.setReadTimeout(20000);
        v.setRequestProperty("User-Agent", "AutostradeInfo-Android");
        v.setRequestProperty("Accept", "application/rss+xml, application/xml, text/xml");
        try {
            if (v.getResponseCode() != 200) throw new Exception("HTTP " + v.getResponseCode());
            StringBuilder sb = new StringBuilder();
            try (BufferedReader r = new BufferedReader(
                    new InputStreamReader(v.getInputStream(), StandardCharsets.UTF_8))) {
                String red;
                while ((red = r.readLine()) != null) sb.append(red).append('\n');
            }
            return sb.toString();
        } finally {
            v.disconnect();
        }
    }
}
