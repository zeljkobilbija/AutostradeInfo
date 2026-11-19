/*
 * Zvanični kilometri izlaza sa italijanske Vikipedije ("Tabella percorso" svake autostrade) → "kal" u podacima.
 *
 *   node alati/kilometraza.js alati/osm/milestone_it.json   (prvo table iz OSM-a)
 *   node alati/km_izlazi.js                                 (pa ovo)
 *
 * Stranice (wikitext) su u alati/wiki/aNN.wiki: index.php?title=Autostrada_ANN_(Italia)&action=raw
 * (API ima strogo ograničenje broja zahteva, action=raw ne; pauza ~1,5 s između stranica).
 * U OSM-u Italije table su retke (A22 nijedna), a Vikipedija ima km za skoro svaki izlaz i odmorište.
 *
 * Redovi tabele se uparuju sa našim stavkama po imenu; par (naš km, km sa Vikipedije) važi samo ako
 * bar jedan drugi par 2–80 km dalje ima isti nagib ±1 (tako ispadaju pogrešna uparivanja, krakovi
 * i deonice sa drugom kilometražom). Ako autostrada ima bar 3 takva para, "kal" = ti parovi + table
 * iz OSM-a koje se sa njima slažu (do 1,5 km); inače ostaje "kal" iz kilometraza.js.
 */
'use strict';
const fs = require('fs'), path = require('path');

const ASSETS = path.join(__dirname, '..', 'app', 'src', 'main', 'assets', 'autoputevi');
const WIKI = path.join(__dirname, 'wiki');

// ---- 1. Redovi tabela iz wikiteksta: [naziv, km] ----
function ocisti(s) {
  let t = s.replace(/<ref[^>]*\/>/g, '').replace(/<ref[\s\S]*?<\/ref>/g, '');
  for (let i = 0; i < 4; i++) t = t.replace(/\{\{[^{}]*\}\}/g, ' ');
  t = t.replace(/\[\[(?:File|Immagine|Image):[^\]]*\]\]/gi, ' ')
    .replace(/\[\[[^\]|]*\|([^\]]*)\]\]/g, '$1').replace(/\[\[([^\]]*)\]\]/g, '$1')
    .replace(/<[^>]+>/g, ' ').replace(/'''?/g, '').replace(/&nbsp;/g, ' ');
  return t.replace(/\s+/g, ' ').trim();
}
const bezAtributa = c => {
  // "align="right" |7,7" → "7,7" (atributi pre jedne crte, ali ne unutar [[…|…]])
  const m = c.match(/^\s*((?:[a-z-]+\s*=\s*"[^"]*"\s*)+)\|(?!\|)([\s\S]*)$/i);
  return m ? m[2] : c;
};
function redovi(tekst) {
  const out = [];
  // zvanični početak (polje "inizio"): kilometraža na tablama raste od njega
  const inizio = ocisti((tekst.match(/\n\|\s*inizio\s*=([^\n]*)/) || [, ''])[1]);
  for (const tab of tekst.match(/\{\|[\s\S]*?\n\|\}/g) || []) {
    if (!/km/i.test(tab)) continue;
    const dvaSmera = /↑\s*km\s*↑/.test(tab);
    const tr = [];
    for (const red of tab.split(/\n\|-[^\n]*/)) {
      const linije = red.split('\n').filter(l => /^\s*\|(?![-}])/.test(l));
      if (!linije.length) continue;
      const celije = linije.map(l => l.replace(/^\s*\|/, '')).join('||').split('||').map(bezAtributa);
      if (celije.length < 3) continue;
      // naziv = prva ćelija sa slovima posle ikonice; km = prvi čisti brojevi posle nje
      let ni = -1;
      for (let i = 1; i < celije.length; i++) if (/[a-zà-ú]{3}/i.test(ocisti(celije[i]))) { ni = i; break; }
      if (ni < 0) continue;
      const km = [];
      for (let i = ni + 1; i < celije.length && km.length < 2; i++) {
        const m = ocisti(celije[i]).match(/^(\d{1,3}(?:[.,]\d{1,3})?)$/);
        if (m) km.push(parseFloat(m[1].replace(',', '.')));
      }
      if (!km.length) continue;
      tr.push([ocisti(celije[ni]), km[0], km[1]]);
    }
    if (!tr.length) continue;
    // ↓km↓ ide redom tabele; ↑km↑ obrnuto. Ako je zvanični početak na dnu tabele, važi ↑km↑.
    const obrnuto = dvaSmera && inizio && slicnost(inizio, tr[tr.length - 1][0]) > slicnost(inizio, tr[0][0]);
    for (const [ime, a, b] of tr) {
      const km = obrnuto ? b : a;
      if (km != null && isFinite(km)) out.push([ime, km]);
    }
  }
  return out;
}

// ---- 2. Uparivanje po imenu ----
const STOP = new Set(['di', 'del', 'della', 'dei', 'delle', 'd', 'e', 'a', 'al', 'in', 'san', 'santa', 'sant',
  'area', 'servizio', 'parcheggio', 'svincolo', 'uscita', 'casello', 'barriera', 'allacciamento', 'raccordo',
  'interconnessione', 'diramazione', 'tangenziale', 'autostrada', 'via', 'km', 'aire', 'sp', 'ss', 'sr']);
const reci = s => s.normalize('NFD').replace(/[̀-ͯ]/g, '').toLowerCase()
  .split(/[^a-z0-9]+/).filter(w => w.length >= 2 && !STOP.has(w) && !/^a\d+$/.test(w));
function slicnost(a, b) {
  const A = new Set(reci(a)), B = new Set(reci(b));
  if (!A.size || !B.size) return 0;
  let z = 0;
  for (const w of A) if (B.has(w)) z++;
  // strana u nazivu (nord/sud/centro/est/ovest) mora da se slaže ako je imaju oba
  const SM = ['nord', 'sud', 'centro', 'est', 'ovest'];
  const sa = SM.filter(w => A.has(w)).join(), sb = SM.filter(w => B.has(w)).join();
  if (sa && sb && sa !== sb) return 0;
  if ((sa || sb) && !(sa && sb) && z <= 1 && Math.min(A.size, B.size) <= 1) return 0.5;   // "Trento" ≠ "Trento sud"
  return z / Math.min(A.size, B.size) - 0.01 * Math.abs(A.size - B.size);
}

// provera čitanja: node alati/km_izlazi.js --redovi a22
if (process.argv[2] === '--redovi') {
  for (const [ime, km] of redovi(fs.readFileSync(path.join(WIKI, process.argv[3] + '.wiki'), 'utf8'))) console.log(km, ime);
  process.exit(0);
}

// ---- 3. Po autostradi ----
const izvestaj = [];
for (const f of fs.readdirSync(ASSETS).filter(f => f.endsWith('.json'))) {
  const p = path.join(ASSETS, f);
  const wp = path.join(WIKI, f.replace('.json', '.wiki'));
  if (!fs.existsSync(wp)) { izvestaj.push(`${f}: nema stranice`); continue; }
  const d = JSON.parse(fs.readFileSync(p, 'utf8'));
  if (d.sekcije.length !== 1) { izvestaj.push(`${f}: ${d.sekcije.length} deonice, preskočeno`); continue; }
  const sek = d.sekcije[0];
  const wr = redovi(fs.readFileSync(wp, 'utf8'));
  const nase = d.stavke.filter(x => x.n);
  const parovi = [];
  for (const [ime, wkm] of wr) {
    let naj = 0, ko = null, drugi = 0;
    for (const x of nase) {
      const s = slicnost(ime, x.n);
      if (s > naj) { drugi = naj; naj = s; ko = x; } else if (s > drugi) drugi = s;
    }
    if (ko && naj >= 0.6) parovi.push([ko.km, wkm, ime, ko.n]);
  }
  // provera nagiba sa drugim parovima
  const oceni = lista => lista.map(([n, z]) => {
    let plus = 0, minus = 0;
    for (const [qn, qz] of lista) {
      const dn = qn - n;
      if (Math.abs(dn) < 2 || Math.abs(dn) > 80) continue;
      const tol = Math.max(1.5, 0.04 * Math.abs(dn));
      if (Math.abs((qz - z) - dn) <= tol) plus++;
      else if (Math.abs((qz - z) + dn) <= tol) minus++;
    }
    return plus === minus ? 0 : (plus > minus ? 1 : -1) * Math.max(plus, minus);
  });
  let lista = parovi;
  for (let k = 0; k < 2; k++) {
    const o = oceni(lista);
    lista = lista.filter((_, i) => o[i] !== 0).map((q, i) => q);
  }
  const o = oceni(lista);
  let kal = lista.map((q, i) => [+q[0].toFixed(2), +q[1].toFixed(1), Math.sign(o[i])]).filter(q => q[2] !== 0);
  kal.sort((a, b) => a[0] - b[0]);
  kal = kal.filter((q, i) => i === 0 || q[0] - kal[i - 1][0] > 0.3);
  // izlaz koji se ne slaže ni sa jednim susedom (npr. A22 Vipiteno 20 pored 16) ispada
  const slaze = (a, b) => a[2] === b[2] && Math.abs((b[1] - a[1]) - a[2] * (b[0] - a[0])) <= Math.max(1.5, 0.04 * Math.abs(b[0] - a[0]));
  for (let k = 0; k < 3; k++) kal = kal.filter((q, i) => (i > 0 && slaze(kal[i - 1], q)) || (i < kal.length - 1 && slaze(q, kal[i + 1])));
  if (kal.length < 3) { izvestaj.push(`${f}: Vikipedija ${wr.length} redova, upareno ${parovi.length}, važi ${kal.length} – ostaju table iz OSM-a (${(sek.kal || []).length})`); continue; }
  // table iz OSM-a koje se slažu sa Vikipedijom
  const pred = n => { let b = kal[0]; for (const q of kal) if (Math.abs(q[0] - n) < Math.abs(b[0] - n)) b = q; return b[1] + b[2] * (n - b[0]); };
  const sveOsm = (sek.kal || []).filter(q => !kal.some(w => Math.abs(w[0] - q[0]) < 0.3));
  const osm = sveOsm.filter(q => Math.abs(pred(q[0]) - q[1]) <= 1.5);
  if (process.env.DEBUG === d.id) {
    console.log('Vikipedija:', kal.map(q => q.join('/')).join(' '));
    console.log('OSM (naš/tabla/predviđeno):', sveOsm.map(q => `${q[0]}/${q[1]}/${pred(q[0]).toFixed(1)}`).join(' '));
  }
  sek.kal = [...kal, ...osm].sort((a, b) => a[0] - b[0]);
  const L = Math.max(...d.stavke.map(x => x.km));
  let rupa = Math.max(kal[0][0], L - kal[kal.length - 1][0]);
  for (let i = 1; i < kal.length; i++) rupa = Math.max(rupa, kal[i][0] - kal[i - 1][0]);
  izvestaj.push(`${f}: Vikipedija ${wr.length} redova, upareno ${parovi.length}, važi ${kal.length} (+${osm.length} od ${sveOsm.length} tabli iz OSM-a), najveća rupa ${rupa.toFixed(0)} km`);
  fs.writeFileSync(p, JSON.stringify(d));
}
console.log(izvestaj.join('\n'));
