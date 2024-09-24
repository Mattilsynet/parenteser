:page/title Skriv bedre commits
:blog-post/author {:person/id :person/christian}
:blog-post/published #time/ldt "2024-09-24T09:00:00"
:blog-post/tags [:metodikk]
:open-graph/image "/images/git.png"
:blog-post/description

Git-historikk kan by på verdifull kontekst som hjelper oss å forstå hvorfor
koden vår er som den er. Særlig hvis den består av gode commits. Men hva utgjør
en god commit?

:blog-post/body

En godt skjøttet git-historikk byr på rikholdig metadata om koden vår, og kan si
oss noe om hvorfor den er som den er. I hvert fall så lenge vi lager gode
commits. Men hva er en god commit? Det er det sikkert mange meninger om, og her
får du mine.

## Omfang: lite og fokusert

En god commit inneholder én logisk endring/ett tillegg. Det betyr som regel at
den også er liten, uten at det er et krav. Det lett å skrive en god
commit-melding til slike commits, og de er lette å lese og forstå.

Med fokuserte commits fungerer commit-loggen også som utvidet dokumentasjon av
koden ved at man kan finne commits som berører et stykke kode og lese mer om
endringene det har vært gjennom.

Hvis teamet ditt praktiserer pull requests ([ikke anbefalt](/pull-requests/)) må
du for guds skyld ikke squashe commits når du merger. Da forsvinner all
historikk, og du sitter igjen med en enorm commit som gjør alle tingene. Hvis
historikken din blir bedre av å squashe commits har jeg noen tips til deg mot
slutten av dette innlegget.

## Commit-meldinger: Effekt og begrunnelse

Endringssettet i en commit forteller meg _hva_ den gjør. Commit-meldingen skal
fortelle meg hvorfor, og hva som er målet. En commit-melding som kun oppsummerer
endringene er verdiløs.

### Tittelen

Den første linja i commit-meldingen bør kort og konsist si hva effekten av
commiten er. Git anbefaler 50 tegn, men det er ille kort. Vi forsøker å holde
den under 70 tegn.

En gang i tiden brukte jeg en [commit
template](https://git-scm.com/book/en/v2/Customizing-Git-Git-Configuration#_commit_template)
som ga følgende prompt for tittel-linjen i commit-meldingen:

```
# If this commit is applied, it will:
```

Dette er et godt oppspark, for det setter deg i modus for å skrive en aktiv og
imperativ tittel. Disse leser godt når du senere ser over loggen:

```
Legg til en feed for å hente personalia for en bruker
Legg til folk-db collector
Gjør det mulig å hente parametere fra ring-requesten
Gjør feeds sine queries dynamiske
Ta vare på siste request i en dynamisk binding i repl-et
Sorter oversikten på forsiden etter antall steder og tilsyn
Støtt at sources gir nil som resultat
Legg sesjon rett på requesten
Ta vare på datascript-conn og re-render klienten etter endringer
```

Alle disse sier noe om hva som er effekten av commiten, uten å bable om de
konkrete endringene som jeg allikevel kan lese ut av diffen. Et mindre poeng er
at meldingene har stor forbokstav og ikke punktum. Det er sånn man skriver
titler.

### Resten av meldingen

Etter tittelen kan vi ha en "body" når det trengs. Langt fra alle commits
trenger denne. Den bør brukes til å si noe om _hvorfor_ vi gjør akkurat de
endringene vi gjør. Det dekker alt fra "hvorfor denne committen?" til "hvorfor på
akkurat denne måten?"

Ofte kan forklaringer som kunne gjort seg som en kommentar i koden gjøre seg
godt i en commit-melding. Denne følger nemlig livsløpet til koden. Endrer du
koden slik at "kommentaren" ikke lenger er aktuell trenger du ikke å huske på å
fjerne den: `git blame` peker deg til den nyeste commiten, hvor den opprinnelige
forklaringen sannsynligvis ikke opptrer.

### Noen eksempler

La oss se noen eksempler på hva jeg mener er gode commit-meldinger:

```
commit a7e457b8f0d6d898cc94592d2e34ece51b130807
Author: Magnar Sveen <magnar.sveen@mattilsynet.no>
Date:   Mon Sep 23 15:18:25 2024 +0200

    Støtt at sources gir nil som resultat

    Dette er først nyttig når man har optional data, som er på vei. I mellomtiden
    sørger det for at man får en bedre feilmelding.
```

Tittelen presenterer effekten av kode-endringen, og teksten forklarer hvorfor vi
ønsker dette. Dette er nyttig informasjon å finne fra `git blame` på den
aktuelle koden.

```
commit da5e75e2320c230575578a261bf94047060ef549
Author: Christian Johansen <christian.johansen@mattilsynet.no>
Date:   Fri Sep 20 15:17:19 2024 +0200

    Dokumenter kommandosystemet
```

Noen ganger er det litt som Forest Gump sa: "And that's all I have to say about
that".

```
commit 2f4de809982f383cd86a3da1b9df1f75e518fcd4
Author: Magnar Sveen <magnar.sveen@mattilsynet.no>
Date:   Wed Sep 18 11:43:33 2024 +0200

    La import-jobben også få være med på alle ENV-secrets

    Det er fordi import-jobben rett og slett bare er en ny deploy av app-serveren.
    Den trenger dermed alle de samme tingene som app-serveren trenger. Burde de
    brytes opp og defineres tydeligere? Godt spørsmål.
```

Igjen får vi forklart effekten av endringene i tittelen og hensikten i teksten
under. Her stilles det i tillegg spørsmål ved om dette er den beste måten å løse
dette på. Det peker på at vi nok trenger å dokumentere den beslutningen (med
grunnlaget) et sted, eller til og med bør revurdere den. Uansett er dette nyttig
kontekst for den som finner den aktuelle koden og tenker "varfor gör dom på
detta viset?"

### Verktøy-støtte

Mange liker å hekte på verktøyene sine i commit-meldingen. Det kan være ting som
[conventional commits](https://www.conventionalcommits.org/en/v1.0.0/), lenker
til issues i Jira/Github, eller meldinger til andre verktøy som følger med på
commits.

**Commit-loggen er der først og fremst for menneskene som jobber med koden**.
Det er helt ok å slenge på litt teknikaliteter for å få det meste ut av
verktøyene sine, men alt sånt bør ligge nederst i commit-meldingen, og bør ikke
være meningsbærende.

At verktøy-koblingene ikke skal være meningsbærende betyr at et saksnummer i et
eksternt system ikke skal brukes i stedet for en god forklaring, men i tillegg.
Ikke alle som leser commit-loggen vil ha tilgang til tredjeparter, og den
eksterne informasjonen har garantert ikke samme levetid eller persistens som en
git commit.

Å bruke av den ekstremt begrensede plassen i tittelen til ting som conventional
commits har i mine øyne lite for seg. Det bedrer ikke forståelsen for mennesker,
en datamaskin klarer å lese nøkkelord hvor som helst, og verst av alt: det
tvinger deg til å skrive ting som et program lett kan finne ut (at noe er en
endring i dokumentasjon er ikke noe jeg burde trenge å fortelle en datamaskin).

## Tips og triks

Til slutt vil jeg nevne noen nyttige triks for å lage gode commits.

### Stage hunks for å bygge atomiske commits

Vi klarer ikke alltid å være så strukturerte at vi kun gjør én hyperfokusert
endring om gangen. Noen ganger glemmer vi å commite underveis. Andre ganger vet
vi ikke helt hvor vi skal, men ser i etterkant at vi har gjort tre forskjellige
ting. Det er ingen grunn til å ikke ha atomiske commits.

`git add -p` lar deg velge individuelle "hunks" fra en fil til en commit. På
denne måten kan vi plukke alle hunks som logisk hører sammen og lage en commit,
og gjenta øvelsen så mange ganger som nødvendig. Det finnes mange grafiske
verktøy som letter bruken av denne, så som [magit](https://magit.vc/).

Senest i går jobbet Magnar og jeg med utformingen av et nytt hjørne i systemet
vårt. Det ble litt prøving og feiling før vi til slutt landet en løsning.
Resultatet ble intet mindre enn 10 commits på rappen når økten var over. Om du
kun leser commit-loggen ser det ut som om vi visste akkurat hvor vi skulle og
tok stødig skritt etter stødig skritt. Ikke helt hva som skjedde. Faktisk kom vi
ikke engang i mål med det vi starta på.

Git-loggen skal være til hjelp for de som leser koden -- ikke en nøyaktig
gjengivelse av hvordan koden ble til.

### Bruk reset og rebase flittig

I samme kategori som over vil jeg anbefale å bruke både reset og rebase flittig.

Reset lar deg fjerne commits du har laget uten å miste endringene i koden, slik
at du kan lage nye commits. Det kan være nyttig når du har en del commits som
ikke er pusha enda, og sitter på fornyet innsikt i arbeidet du har gjort.

Rebase lar deg gjøre en litt mindre justering av commits du allerede har laget
-- endre litt på teksten i en commit, legge til en test-fil du glemte på en
commit som ligger litt ned i loggen osv. Du bør helst ikke rebase commits som er
pushet til main-branchen.

Dersom du i dag squasher PR-er så kan det være at løsningen du ser etter er en
reset (og nye commits) eller en rebase for å rydde opp i historikken i branchen
før den merges med full historikk til main-branchen.

## Teori og praksis

Det var noen tanker om hva som utgjør gode commits. Om jeg får det til i praksis
er en annen ting, og noe
[du](https://github.com/Mattilsynet/matvaretabellen-deux/commits/main)
[kan](https://github.com/Mattilsynet/smilefjes-deux/commits/main)
[bedømme](https://github.com/cjohansen/replicant/commits/main)
[selv](https://github.com/cjohansen/portfolio/commits/main), men da vet du i det
minste hva jeg prøver på.
