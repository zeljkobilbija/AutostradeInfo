package com.zeljkobilbija001.autostradeinfo;

/** Neprekinuti deo autoputa. Kilometri se računaju od njegovog početka. */
public final class Deonica {
    public final String od, doMesta;
    /** Kuda se ide kada kilometri rastu (+) i kada opadaju (-). */
    public final String plus, minus;

    public Deonica(String od, String doMesta, String plus, String minus) {
        this.od = od;
        this.doMesta = doMesta;
        this.plus = plus;
        this.minus = minus;
    }

    /** Mesta gde je autoput stvarno prekinut: parovi [km kraja, km nastavka]; tu se linija ne spaja. */
    double[] prekidOd = new double[0], prekidDo = new double[0];

    /** Da li je između dva kilometra prekid autoputa. */
    public boolean prekidIzmedju(double a, double b) {
        double od = Math.min(a, b), d = Math.max(a, b);
        for (int i = 0; i < prekidOd.length; i++) {
            if (prekidOd[i] >= od - 0.05 && prekidDo[i] <= d + 0.05) return true;
        }
        return false;
    }

    public String naziv() {
        return od + " – " + doMesta;
    }
}
