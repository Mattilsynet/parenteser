:page/title Knotinuerlig indignert
:blog-post/published #time/ldt "2023-12-08T09:00:00"
:blog-post/author {:person/id :person/christian}
:blog-post/tags [:samarbeid :metodikk]
:blog-post/description

Står pull requests i kontrast til kontinuerlig integrasjon? Er kontinuerlig
integrasjon det samme som CI? Hva med CI/CD? Jeg forsøker å oppklare noen
uklarheter fra mitt forrige innlegg om pull requests.

:open-graph/description

Står pull requests i kontrast til kontinuerlig integrasjon? Det trenger vi noen
definisjoner for å svare på.

:blog-post/body

For å diskutere i hvilken grad pull requests og kontinuerlig integrasjon går
sammen kan det være lurt å være enige om noen definisjoner. Som vanlig er det få
autorative definisjoner å oppdrive, så det beste jeg kan få til er å peke på hva
det betyr for meg, og hvilke kilder som har informert den oppfatningen.

## Kontinuerlig integrasjon

Kontinuerlig integrasjon -- eller ["continuous
integration"](https://en.wikipedia.org/wiki/Continuous_integration)/CI, på godt
fagnorsk -- er prosessen der alle utviklerne som jobber på samme kodebase
kontinuerlig fletter sammen endringene sine. Begrepet dukket opp allerede i
1991, men forventningene til "kontinuerlig" har endret seg dramatisk fra den
gangen. "Extreme programming"/XP-bevegelsen trykket begrepet til sitt bryst, og
var tydelige på at "kontinuerlig" betyr "mange ganger om dagen".

I bunn og grunn er dette alt. En arbeidsprosess der man etterstreber å sitte med
så lik versjon av kode på hver sin maskin som mulig. Hensikten er å slippe å
bruke tid på merge-konflikter, unngå duplisert arbeid, ha delt
virkelighetsforståelse, få raskere utveksling av arbeid ("jeg bruker funksjonen
du skrev i stad i denne featuren nå!") og legge til rette for å få ting raskere
ut i produksjon.

## Hva med CI-systemet mitt?

Klar for nok en IT-bransjeklassiker? Mange verktøyleverandører har også trykket
"CI" til sitt bryst og selger deg villig CI-systemer. Resultatet er at for mange
så betyr continuous integration/CI/kontinuerlig integrasjon rett og slett
"automatiserte bygg".

Systemer som eksempelvis Github Actions kan hjelpe team med å automatisere
arbeidsflyten sin, men gir ikke i seg selv kontinuerlig integrasjon. Du kan ikke
kjøpe eller installere CI som et produkt noe mer enn du kan kjøpe eller
installere "agile".

En arbeidsprosess der alle jobber på hver sin branch, jevnlig får merge
konflikter og sjelden leverer til produksjon blir ikke "CI" av å få et
automatisert bygg kasta etter seg.

## CI/CD?

Noen systemer nøyer seg ikke med å bare være CI. De er CD også. CD står for
"continuous delivery" -- nok en prosess som av mange forveksles med et system
man kan kjøpe inn.

Kontinuerlig leveranse (CD) er den naturlige forlengelsen av kontinuerlig
integrasjon. Når all koden flettes sammen løpende kan vi også levere løpende. CD
handler om å gå fra milepæls-leveranser ("prodsett i slutten av hver sprint",
"release en gang i måned", etc) til kontinuerlig leveranser -- til
produksjonsmiljøet. Mange ganger om dagen. Helst via automatiserte bygg.

Igjen: du får ikke kontinuerlig leveranse av å sette opp masse automatiserte
prosesser som oppdaterer testmiljøet ditt. Kontinuerlig leveranser er prosessen
å jevnlig levere til dine brukere.

## Pull requester da?

Er det mulig å kombinere PR med kontinuerlig integrasjon? Selve definisjonen på
denne arbeidsflyten er at alles endringer flettes sammen løpende. Når du
committer til en branch og merger den til main via en PR så har du dermed per
definisjon ikke kontinuerlig integrasjon.

CI er ikke nødvendigvis enten eller, det er heller en skala. Som et kompromiss
kan jeg være med på at dersom PR-ene dine kommer fra brancher som i snitt lever
kortere enn én dag, og ikke krever manuell oppfølging, så er du ganske nær på
målet. Men jeg tror allikevel at du ville oppleve en bedre flyt uten PR, men det
får bli en annen bloggpost.

Avslutningsvis vil jeg anbefale [The DevOps
Handbook](https://www.amazon.com/DevOps-Handbook-World-Class-Reliability-Organizations/dp/1950508404),
en bok som går dypere inn i disse begrepene, og ikke minst
[Accelerate](https://www.amazon.com/Accelerate-Software-Performing-Technology-Organizations/dp/1942788339),
som viser hvordan forskning(!) viser at de gir gode resultater langs flere
akser.
