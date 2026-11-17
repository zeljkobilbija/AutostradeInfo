/*
 * Autostrada u jednom komadu (kao AutobahnInfo, alati/kilometraza.js → spojiDeonice):
 *
 *   node alati/spoji_deonice.js            → spaja sve deonice u assets/autoputevi/*.json
 *   node alati/spoji_deonice.js --suvo     → samo izveštaj
 *
 * Sve deonice jedne autostrade postaju jedna lista, redom kojim idu. Deonica okrenuta suprotno od
 * prethodne se okreće (km, strana, plus/minus). Kilometri se nastavljaju (dodaje se vazdušna razdaljina
 * preko prekida). Gde je autostrada stvarno prekinuta (razmak > 5 km) upisuje se "prekidi":
 * [[km kraja, km nastavka]] – aplikacija tu ne spaja liniju. Ponovno pokretanje ne menja već spojeno.
 * Pokretati posle autostrade_skripta.js (ona pravi deonice iznova).
 */
'use strict';
const fs = require('fs'), path = require('path');

const ASSETS = path.join(__dirname, '..', 'app', 'src', 'main', 'assets', 'autoputevi');
const suvo = process.argv.includes('--suvo');
const KX = la => 111.32 * Math.cos(la * Math.PI / 180), KY = 110.57;
const izvestaj = [];
/** Ne spajaju se: A90 (GRA) je prsten, a njegova dva dela su verovatno dva kolovoza istog prstena. */
const IZUZECI = new Set(['A90']);

function spojiDeonice(d) {
  if (d.sekcije.length < 2 || IZUZECI.has(d.id)) return false;
  const tac = si => d.stavke.filter(x => x.si === si && x.la != null);
  const dist = (a, b) => Math.hypot((a.la - b.la) * KY, (a.lo - b.lo) * KX(a.la));
  const krajevi = si => { const a = tac(si); if (!a.length) return null;
    return [a.reduce((m, x) => x.km < m.km ? x : m), a.reduce((m, x) => x.km > m.km ? x : m)]; };
  const duzina = si => Math.max(0, ...d.stavke.filter(x => x.si === si).map(x => x.km));
  const prekidi = [];
  let pomak = 0, prethodniKraj = null;
  const nova = { ...d.sekcije[0] };
  for (let si = 0; si < d.sekcije.length; si++) {
    const s = d.sekcije[si], L = duzina(si);
    const kr = krajevi(si);
    // okreće se ako mu je kraj bliži kraju prethodne nego početak
    const okreni = !!(prethodniKraj && kr && dist(prethodniKraj, kr[1]) < dist(prethodniKraj, kr[0]));
    if (si > 0) {
      const pocetak = kr ? (okreni ? kr[1] : kr[0]) : null;
      const razmak = prethodniKraj && pocetak ? dist(prethodniKraj, pocetak) : 0;
      if (razmak > 5) prekidi.push([+pomak.toFixed(1), +(pomak + razmak).toFixed(1)]);
      pomak += razmak;
      nova.do = okreni ? s.od : s.do;
      nova.plus = okreni ? s.minus : s.plus;
      izvestaj.push(`${d.id}: + ${s.od} – ${s.do}${okreni ? ' (okrenuta)' : ''}, razmak ${razmak.toFixed(1)} km`);
    }
    for (const x of d.stavke) {
      if (x.si !== si) continue;
      x.km = +(pomak + (okreni ? L - x.km : x.km)).toFixed(1);
      if (okreni && x.s) x.s = x.s === '+' ? '-' : '+'; // izlazi nemaju stranu
      x.si = 0;
    }
    if (kr) prethodniKraj = okreni ? kr[0] : kr[1];
    pomak += L;
  }
  if (prekidi.length) nova.prekidi = prekidi;
  d.sekcije = [nova];
  d.stavke.sort((x, y) => x.km - y.km);
  return true;
}

let n = 0;
for (const f of fs.readdirSync(ASSETS).filter(f => f.endsWith('.json')).sort()) {
  const p = path.join(ASSETS, f), d = JSON.parse(fs.readFileSync(p, 'utf8'));
  if (!spojiDeonice(d)) continue;
  n++;
  const s = d.sekcije[0];
  izvestaj.push(`${d.id} → ${s.od} – ${s.do}${s.prekidi ? ', prekidi ' + JSON.stringify(s.prekidi) : ''}`);
  if (!suvo) fs.writeFileSync(p, JSON.stringify(d));
}
console.log(izvestaj.join('\n'));
console.log(`spojeno autostrada: ${n}${suvo ? ' (suvo, ništa nije snimljeno)' : ''}`);
