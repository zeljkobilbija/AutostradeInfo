# Autostrade Info – Play Console

Paket: `com.zeljkobilbija001.autostradeinfo` · prva verzija 1.0 (versionCode 1)
Postupak: podsetnik, poglavlje 4a (A → G). AdMob: JEDNA aplikacija, posle objave „Aggiungi store“ na istoj.

## Crea app
- Nome dell'app: **Autostrade Info: uscite e aree** (iz `play/opis/it-IT.txt`)
- Lingua predefinita: **Italiano – it-IT**
- App o gioco: **App** · Senza costi o a pagamento: **Senza costi** (kupovina „Bez reklama“ je unutar aplikacije)
- Čekirati izjave (Norme del programma per gli sviluppatori, leggi sull'esportazione USA)

## Listing prodavnice (Scheda dello store predefinita)
- Tekstovi: `play/opis/it-IT.txt` (glavni), `play/opis/en-US.txt` (Gestisci traduzioni → Inglese (Stati Uniti) – en-US)
- Ikona 512×512: `play/grafika/ikona_512.png`
- Grafica in primo piano 1024×500: `play/grafika/naslovna_1024x500.png`
- Snimci ekrana telefona: `play/snimci/` (pravi ih Claude kad je telefon slobodan)
- Categoria: **Mappe e navigatori** · Tag po želji: Viaggi, Navigazione
- Email za kontakt: isti kao za ostale aplikacije · Sito web: `https://zeljkobilbija.github.io`

## Contenuti dell'app (`…/app/<broj aplikacije>/app-content`)
| Stavka | Odgovor |
|---|---|
| Norme sulla privacy | `https://zeljkobilbija.github.io/autostradeinfo/privacy.html` |
| Annunci | **Sì, contiene annunci** |
| ID pubblicità | **Sì** → Pubblicità o marketing |
| Accesso all'app | Tutte le funzionalità disponibili senza restrizioni |
| Pubblico di destinazione | **18+** (non per bambini) |
| Classificazione dei contenuti | Upitnik: kategorija „Tutte le altre app“ / uslužna; bez nasilja, seksa, kockanja, korisničkog sadržaja; **ne deli lokaciju sa drugim korisnicima**; ima kupovinu u aplikaciji |
| App di notizie | No |
| App governative | No (podaci CCISS su javni, aplikacija nije vladina) |
| Funzionalità finanziarie / Salute | Nessuna |

### Autorizzazioni del servizio in primo piano (FOREGROUND_SERVICE_LOCATION – Vožnja)
- Tip: **Posizione** → čekirati samo **Navigazione**
- Opis (kopirati):
  `Driving mode shows traffic warnings for the motorway the user is driving on; the user starts it with a button and sees a permanent notification; it stops when the user taps Stop or closes the app.`
- **Video** (YouTube, „Non in elenco“), 20–30 s snimak ekrana telefona: početni ekran → „Avvia“ (Modalità guida) → obaveštenje gore → otvoren ekran autostrade → „Stop“. Može i kod kuće (pokazuje pokretanje i obaveštenje).

### Sicurezza dei dati (Data safety) – kao InterstateInfo + lokacija + kupovina
- Korak 2: raccoglie/condivide **Sì**; crittografia in transito **Sì**; „La mia app non consente agli utenti di creare un account“; account esterni **No**; eliminazione dati **No**. Usamljeno „No“ ispod „Rimuovi le risposte“ NE DIRATI
- Korak 3, tipovi:
  - Posizione: **approssimativa** i **precisa** (Modalità guida; obrađuje se na telefonu; AdMob približna)
  - Informazioni personali: **Indirizzo email** (Google ga „vidi“ u bibliotekama AdMob/Play services/Billing; bez toga automatska provera odbija)
  - Informazioni finanziarie: **Cronologia acquisti** (Google Play Billing, „Rimuovi annunci“)
  - Attività nelle app: **Interazioni con l'app**
  - Informazioni e prestazioni dell'app: **Log degli arresti anomali**, **Diagnostica**
  - ID dispositivo o altri ID
- Korak 4 za svaki: Raccolti + Condivisi; nije privremeno; obavezno (email: korisnik može da bira); svrhe: Pubblicità o marketing, Analisi, Prevenzione di frodi, sicurezza e conformità; za lokaciju i dodatno **Funzionalità dell'app**; za kupovinu **Funzionalità dell'app**

## Note di rilascio (prva verzija)
```
<it-IT>
Prima versione: uscite, aree di servizio, distributori con prezzi medi, parcheggi e servizi su 47 autostrade italiane, in ordine di chilometro. Modalità guida con chilometro e direzione, traffico in tempo reale dal CCISS. Funziona offline.
</it-IT>
<en-US>
First release: exits, service areas, fuel stations with average prices, parking and facilities on 47 Italian motorways, sorted by kilometre. Driving mode with kilometre and direction, live traffic from CCISS. Works offline.
</en-US>
```

## Proizvod „Bez reklama“ (posle otpremanja verzije sa Billing-om)
- ID prodotto `remove_ads` · Nome `Rimuovi annunci` · Descrizione `Rimuove tutta la pubblicità dall'app con un acquisto unico.`
- Icona: `play/grafika/remove_ads_512.png`
- ID opzione `buy` · Acquista · **2,99 EUR** → Attiva

## Stanje
- [x] R8 + pravila, release proveren na telefonu (20.11.2026)
- [x] Politika privatnosti EN/IT na sajtu (push 20.11.2026)
- [x] Tekstovi IT/EN, ikona, naslovna, ikona remove_ads
- [ ] AdMob (korak A): app, Banner, Interstitial → `app/src/main/res/values/admob.xml`; GDPR poruka
- [ ] Snimci ekrana (`play/snimci/`)
- [ ] Novi ključ: Generate Signed App Bundle → **Create new…** → `keystore-kljucevi\autostradeinfo.jks` (NIKAD na GitHub; rezervna kopija)
- [ ] Play Console: Crea app, listing, Contenuti dell'app, video za Vožnju
- [ ] Test chiusi (Alpha) → remove_ads → probna kupovina → Produzione
- [ ] Posle objave: AdMob „Aggiungi store“ na ISTOJ aplikaciji
