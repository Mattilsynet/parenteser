:page/title "Bygg delt forståelse av kvalitet med parprogrammering"
:blog-post/author {:person/id :person/teodor}
:blog-post/published #time/ldt "2025-01-28T08:30:00"
:blog-post/tags []
:blog-post/description

Jeg vil skrive _god kode_.
Jeg vil være stolt av koden—både koden jeg har skrevet selv, og koden andre har skrevet.
Dit kan man komme med parprogrammering!

:open-graph/description

Hvordan bruker man parprogrammering til å komme til en kodebase alle kan være stolt av?

:blog-post/body

Hva må til for at et team skal kunne jobbe godt sammen og levere høy kvalitet?
Jeg mener at følgende må være på plass:

- **Tillit**: Vi må kunne stole på de andre på laget.
  Både at vi personlig føler oss trygge med de andre, og at vi profesjonelt er trygge på at koden ikke raser utfor et stup hvis vi drar på ferie.
- **Delt intensjon**: vi må ha en formening om hvor vi skal, og være enige om det målet.
- **Delt forståelse av kvalitet**: Vi må ha en formening om hvordan _bra_ ser ut: når det gjelder kode, når det gjelder produkt, når det gjelder design, og når det gjelder problemene teamet jobber med.

Men hvordan får vi tillit, delt intensjon og delt forståelse av kvalitet på plass?
Jeg foreslår følgende:

1. Av tillit, delt intensjon og delt forståelse av kvalitet, start med delt forståelse av kvalitet mellom utviklere.
   Det er håndfast, og øker farten på _alt_.
2. For å bygge delt forståelse av kvalitet, parprogrammer regelmessig.

## Delt forståelse av kvalitet i gårsdagens parprogrammering

I går satt Ole Marius, Christian og jeg sammen.
Jeg har ikke parprogrammert noe særlig med Ole Marius før.

Tiden gikk slik:

1. Én time der Ole Marius, Christian og jeg løser halve problemet

2. Én time der Christian drar i møte, og Ole Marius og jeg diskuterer hvilke av disse to alternativene vi skal gå for:

    ```clojure
    (defn stringify-body [body]
      (cond-> body
        (and (not (string? body))
             (not (nil? body)))
        slurp))
    
    (defn stringify-body [body]
      (cond-> body
        (instance? java.io.Reader body)
        slurp))
    ```

3. Én time der Ole Marius og jeg løser den andre halvdelen av problemet, etter at Christian kom tilbake.

Var én times diskusjon av to linjer kode bortkastet tid?

Jeg vil gi et tydelig _nei_.
Før to personer kan parprogrammere effektivt, må man finne en rytme som funker.
Den rytmen krever at man gjør noen ting raskt, og andre ting sakte.

## Konkrete kodeeksempler øker kvaliteten på diskusjonen

Når man sliter med å komme i gang, kan det være fint å få _noe_ kode på bordet.
Med ett eksempel på fungerende kode, er det lettere å peke på hvor man bør legge inn mer innstats.
Skal man velge mellom to måter å gjøre noe på, kan man skissere ut begge alternativene.

Det gjør diskusjonen konkret:

1. Vi diskuterer to tydelige alternativer
2. Vi skal _kun_ ta valget mellom disse to alternativene akkurat nå.

Når vi har tatt valget, kan vi gå videre.

## Hvis vi ikke er enige om hvor vi skal, kommer vi ikke noen vei ved å kverne ut kode

En annen situasjon man kan havne i er at én person har en plan og vil kjøre på, og den andre ikke henger med.
Hvis Ole Marius vet akkurat hvor han vil hen, og jeg ikke henger med, må vi prate _mer_.
Når vi begge vet hvor vi skal, kan vi lettere komme videre.

## I parprogrammering går noen ting raskere, og andre ting tregere

Når Ole Marius kan et subsystem godt, kan jeg stole på at han har rett, og vi kan hoppe over detaljene.
Det sparer oss et lass med tid.

Men jeg kan ikke umiddelbart hoppe på alle ideene jeg kommer på.
Da kjører jeg av gårde på egenhånd, og kommunikasjon og samarbeid bryter ned.
Da må jeg i stedet kommunisere hvor jeg vil hen—eventuelt notere at det er noe jeg vil se på senere.

Noen ting går raskere, andre ting går tregere!

## Man blir kjent med hverandre over tid

I starten går parprogrammering aldri som smurt.
Når jeg ikke kjenner personen jeg skal parprogrammere med, sitter ikke kommunikasjonen om hvor vi skal, når vi bør kode litt, og når vi bør gjøre et pragmatisk valg for nå.
Det funker gradvis bedre når man legger inn tiden!

Det er lett å si at en times diskusjon om to linjer kode er bortkastet tid.

Men de to linjene _er ikke poenget_.
Poenget er hvilket problem vi skal løse, og hvordan vi skal løse det.
Da blir man bedre ved å trene på parprogrammering sammen.

I større skala: hvis du er på et team som skal designe og bygge et atomkraftverk, er oppgaven vanskelig.
Det er lettere å diskutere [materialene til sykkelskuret](https://en.wikipedia.org/wiki/Law_of_triviality).
Det er ikke trivielt å løse vanskelige problemer sammen.
Men hvis man legger inn litt innsats, blir det bedre etter hvert.

Og når man etter hvert blir enige om hva som er god kode og god prosess for å skrive kode (at man etablerer en delt forståelse av kvalitet), begynner man etter hvert å stole på hverandre (tillit), og det er lettere å diskutere hvor man skal (delt intensjon).

