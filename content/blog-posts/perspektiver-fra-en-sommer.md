:page/title Perspektiver fra en sommer
:blog-post/author {:person/id :person/aurora}
:blog-post/published #time/ldt "2026-07-09T14:47:19.792251"
:blog-post/tags []
:blog-post/description

Denne sommeren har jeg vært så heldig å få begynne å jobbe sammen med folka på
Team Servering. Her er noen refleksjoner rundt det.

:blog-post/body

Heia, bloggen <33

Æ ska ta dokker med på en reise fra IFI og Ole-Johan Dahls hus til Team
Servering på Holberg Terasse; fra nyutdanna informatiker til første deployment
til prod.

#### Fra [IFI](https://www.mn.uio.no/ifi/) til Team Servering
Historien min med Team Servering startet høsten 2025, hvor jeg tok et kurs i
funksjonell programmering i Scheme.

Jeg falt umiddelbart for LISP, hvor alt -- også verdens tilstand --
parameteriseres, og man skriver rene funksjoner, hvor funksjoner er førsteklasses borgere og
kontrollflyt styres av kondisjonale uttrykk fremfor løkker.

Kurset brukte kanskje [verdens beste bok om
programmering](https://en.wikipedia.org/wiki/Structure_and_Interpretation_of_Computer_Programs)
og ble holdt av [kanskje verdens mest engasjerende
foreleser](https://www.mn.uio.no/ifi/om/aktuelt/utmerkelser/arets-foreleser-i-2024.html).

Jeg elsket å programmere i Scheme, men satt igjen med følelsen av at LISP var
best brukt som en intellektuell øvelse i abstraksjon, fremfor et praktisk verktøy
jeg faktisk kom til å få bruk til i arbeidslivet. *Unless..?*

Etter en av forelesningene snakket jeg med Lars om faktiske
anvendelser av funksjonell programmering i næringslivet.
Han nevnte et lite team med utviklere hos Mattilsynet som brukte
en moderne LISP for å utvikle tjenestene sine og pekte meg i retning av nettopp
[denne bloggen](https://parenteser.mattilsynet.io/).

Jeg leste noen innlegg, men det meste føltes ganske fjernt for en student som
ikke hadde et eneste åpent repo på GitHub.

Ønsket om å ikke bli Ekte Voksen helt enda, en generell aversjon til
*Corporate Culture*, heftig tilfelle av *imposter syndrome*, og et ønske om
bedre lønnsnivå in-house gjorde at jeg hadde slått fra meg tanken om å gå rett
ut i jobb etter bachelorgraden. 

Det fristet uansett ikke å jobbe i noen av de store konsulenthusene.

Som en siste krampetrekning søkte jeg på en stilling hos Mattilsynets Team
Eksport. Jeg husket ikke helt om dette var teamet Lars hadde nevnt, men jeg er
uansett positiv til store statlige arbeidsgivere.

Da jeg kom på intervju snakket jeg i ett sett om emnet i funksjonell
programmering hvor vi jobbet med metasirkulær evaluator, og at jeg var helt frelst.
Jeg følte jeg kom nærmere og nærmere kjernen av programmering som
problemløsning.

I etterkant av intervjuet ble jeg invitert tilbake for en samtale med Team Servering.
Atter en gang fikk jeg delt mitt engasjement for LISP.

Jeg var dog litt forvirret. Jeg hadde jo originalt søkt på en stilling som
eksisterte. Hvorfor var jeg nå inne og snakket med et team uten åpne
stillingsutlysninger?

Jeg ble fortalt at jeg "hadde en interessant profil" for Mattilsynet, og det
kanskje kunne være en mulighet på Team Servering.

I utgangspunktet hadde jeg jo bestemt meg for å ta master, så vi
landet på en mellomløsning hvor jeg skal jobbe 50% og ta masteren på deltid.

For å sparke i gang tilværelsen som utvikler på Team Servering har jeg jobbet
100% i sommer, og som siste dag på sommerjobben skal jeg skrive bloggpost.

### Fra første dag til første commit til prod
Dette er min første utvikler-jobb. Læringskurven har vært svært bratt og jeg har
vært overveldet store deler av tiden. 

Jeg har også  tenkt at informatikkstudiet i liten grad har forberedt meg på
livet som arbeidstaker.

Her er en liste over ting jeg syns var overveldende å sette meg inn i:
- Framsideutvikling
- NATS
- GCP
- ny emacs-config og maGit
- Clojure
- Datomic
- MacOS
- emoji-kulturen på Slack ???
- kleskode på kontoret (det er 27 grader, kan jeg ha nakne skuldre???)
- organisasjonen som helhet
- kantina (hvorfor spiser vi egentlig lunsj klokken 11? det er sykt tidlig)

Før jeg begynte i sommer hadde jeg aldri rørt Clojure. Jeg kom inn med
blanke ark, klare for å fargelegges av teamets tilnærming til språket og
teknologien. 

(Jeg hadde forøvrig ikke skrevet funksjonell kode siden høsten 2025
foruten om enkle snutter i Elisp for Emacs-config).

Den første uka gikk mye tid med til praktiske saker og ting, men allerede da
fikk jeg koblet meg på Matnyttig-repoet vårt og startet et REPL.

I starten jobbet jeg mye med å bare sette meg inn i Clojure-syntaks.
Jeg leste [Programming Clojure 4th
ed.](https://pragprog.com/titles/shcloj4/programming-clojure-fourth-edition/)
og gjorde oppgaver.

[4clojure](https://4clojure.oxal.org/#/) har enkle oppgaver med et online REPL
som automatisk kjører testene for deg, mens [Clojure
Camp](https://exercises.clojure.camp/) har litt lengre oppgaver hvor man må ha
et eget REPL kjørende for å teste og bygge ut funksjoner.

For arbeidet med disse oppgavene lagde jeg et navnerom `hei`.

Den første funksjonen i fila ser slik ut:

```clj
(defn process-value [value]
  (cond
    (and (number? value) (> value 10))
    :pretty-big

    (and (number? value) (< value 0))
    :negative

    (and (number? value) (zero? value))
    :zero

    (number? value)
    :small-number

    (string? value)
    :a-string

    :else
    :something-else))
```

Her har vi en helt enkel programflyt som typesjekker en verdi og returnerer et
nøkkelord basert på verdien. 

Allerede her er det konsepter som avviker fra Scheme:
1. Ingen paranteser rundt `<test-clause> <then-clause>`
2. Firkantparanteser `[]` rundt parametrene
3. Nøkkelord `:key`

Punkt 1 er utelukkende å bli vant til syntaks. Rich Hickey har
designet språket slik at overflødige paranteser (den største kritikken mot
LISPer generelt) er strippet vekk. 

Punkt to introduserer vektorer. I vanlig LISP
er paranteser overlastet til å representere mer enn bare lister. Dette er for
eksmepel tilfellet i Scheme.  Rich Hickey
løste dette ved å introdusere en ny type samling, nemlig vektorer.

Punkt tre introduserer nøkkelord, som typisk også introduserer assosiative
strukturer. Jeg husker godt at Teodor viste meg første dagen at
nøkkelordet både kan brukes som et parameter til et map, og som en funksjon som
tar inn et map som parameter:

```clj
(= (:b {:a 1 :b 2})
   ({:a 1 :b 2} :b)
```

Videre utforsket vi selvfølgelig Emacs-configen og særlig Paredit med sine mange
finuerlige tastaturbindinger. Favorittene mine så langt er:

| Binding     | Funksjon | Hva skjer?                        |
|-------------|----------|-----------------------------------|
| `s-<right>` | slurp    | Inkluder neste s-exp i denne      |
| `M-r`       | raise    | Erstatt foreldrenode med meg      |
| `C-M-u`     | up       | Gå opp i syntakstreet             |
| `C-M-d`     | down     | Gå ned til venstre i syntakstreet |
| `C-@`       | expand   | Marker foreldrenode               |

Å gå frem og tilbake mellom s-expressions sier seg selv, men da jeg oppdaget
`up` og `down`for noen dager siden sparte jeg meg for mye ett-tegnsnavigering.

Videre er jeg skikkelig stor fan av `slurp`, men jeg er litt
mer glad i hjemmerekken enn Christian og Magnar, som er ganske komfortabel nede
på piltastene sine. 

Ifølge Christian lagde Magnar [`annoying-arrows-mode`](https://github.com/magnars/annoying-arrows-mode.el) i forsøk på 
slutte å bruke piltastene så mye, men den ble rett og slett for annoying...

For å slippe å bevege høyrehånden min ca 10 cm hver gang jeg vil slurpe i meg
neste s-exp, har jeg lagt til en egen ny binding for slurp, nemlig `s-F`.

#### Min første oppgave i Matnyttig

Etter to uker med lesing og programmering av bittesmå funksjoner som bare gjør
CPUen varm og sjelen kald, spurte jeg Christian om jeg kunne få en mer konkret
oppgave direkte knyttet til Matnyttig. 

Det føltes litt meningsløst å skrive min
tiende filter over en enkel samling. Jeg ville bryne meg på ekte data i et ekte
system. 

Christian kom med to forslag: 
1. Rydd opp i attributten `:midlertidig/ræl`.
2. Forbedre "algoritmen" `bekymringssmelding/finn-nøkkelord`.

Begge oppgavene omhandler behandling av tekstlig data.

Det midlertidige rælet var tidenes sammensurium av flyktighet og rukkel.
Jeg syns det var litt uoversiktelig, da rælets natur varierte; det var uvisst
hva jeg lette etter og hva som kunne være nyttig å trekke ut.

Oppgave 2 hørtes derimot mye mer overkommelig ut. Det finnes 16112
bekymringsmeldinger i databasen på gammelt format hvor dataen i stor grad er
fritekst. 

For å gjøre det lettere for inspektører å få et overblikk over
bekymringsmeldinger på et serveringssted eller hos en kjede er det nyttig å
kunne få vist hvilke nøkkelord en bekymringsmelding handler om.

Den eksisterende "algoritmen" var nokså naiv; En stor cond som tok inn en streng
`melding` med forskjellige nøkkelord som `then`-klausuler og  predikater
bestående av store `or`-uttrykk med
strengoperasjonen `str/includes` og forskjellige søkeord. 

Her er et lite eksempel med noen få nøkkelord og søkeord: 

```clj
(defn finn-nøkkelord [melding]
  (cond
    (or (str/includes? melding "skadedyr" true)
        (str/includes? melding " mus " true))
    "Skadedyr"

    (str/includes? melding "forgift" true)
    "Matforgiftning"))
```

Denne implementasjonen klarte å klassifisere ca 3/4 bekymringsmeldinger, med 1/4
av meldingene som uklassifisert uten noen nøkkelord. 

Først la jeg bare til noen
flere søkeord som disjunkter til de forskjelligere nøkkelordene.

Videre fant jeg ut at vi bare brukte `finn-nøkkelord` med attributten
`:bekymringsmelding/beskrivelse`, selv om to andre felter
`:bekymringsmelding/merknad` og `:bekymringsmelding/andre-opplysninger` også
kunne inneholde relevant informasjon.

Jeg endte også opp med å ønske å fange deler av strengen med regulære uttrykk.
Etter mye frem og tilbake endte vi opp med følgende design på `finn-nøkkelord`:
```clj
(def nøkkelord->søkeord
  {"Nøkkelord" #{#"regexes" #"her" ,,,}
  ...})

(defn hent-tekst [bekymringsmelding]
  (->> (select-keys bekymringsmelding [:bekymringsmelding/beskrivelse
                                       :bekymringsmelding/merknad
                                       :bekymringsmelding/andre-opplysninger] )
       (vals)
       (remove nil?)
       (map str/lower-case)
       (str/join " ")))

(defn finn-nøkkelord-frekvens [bekymringsmelding]
  (let [tekst (hent-tekst bekymringsmelding)]
    (->> nøkkelord->søkeord
         (map (fn [[nøkkelord søkeord]]
                [nøkkelord (->> søkeord
                                (map #(count (re-seq % tekst)))
                                (apply +))]))
         (remove (comp zero? second))
         (into {}))))
     
```

Her har vi et aldeles nydelig skille mellom data og funksjoner. 

Det kan enkelt
legges til nye nøkkelord eller søkeord, og
flere felter kan legges til i hent-tekst dersom bekymringsmeldinger får flere
relevante attributter i fremtiden. 

Funksjonene er klassiske sekvensoperasjoner hvor det faller helt naturlig å
bruke den fantastiske makroen `->>`. 

I løpet av disse fire ukene har jeg blitt skikkelig glad i både `->>`og `->`.
De brukes veldig aktivt her på teamet, og er så fine at Magnar har skrevet en
[bloggpost](https://www.kodemaker.no/blogg/2020-06-thread-first-and-last/) om 
det back in the day.

For bevegelsessiden endte vi opp med en algoritme som finner nøkkelord for over
9/10 bekymringsmeldinger! Nøkkelordene vises på "kortet" til høyre under type:

<img src="/images/sommer26-1.png"
     width="1000"
     style="display: block; margin: 0 auto"
     alt="Mitt serveringssted med syntetiske data">

Det originale målet med oppgaven fra Christian var nådd: En bedre algoritme for
å klassifisere bekymringsmeldinger. 

Løsningen kunne lett bygges videre på, for både serveringssteder og kjeder. 

For et enkelt serveringssted er det lagt til tags øverst som indikerer hvor
mange bekymringsmeldinger som har treff på hvert nøkkelord. 

Her har for eksempel Bølla Burger Bodø slitt med skadedyr:


<img src="/images/sommer26-2.png"
     width="1000"
     style="display: block; margin: 0 auto"
     alt="Mitt serveringssted med syntetiske data">
     
Den samme agregeringen gjøres over alle serveringssteder som tilhører en kjede.
Slik vil det bli lettere å avdekke problemer på tvers i kjeder.

Vi kan for eksempel se at hele kjeden Bølla Burger har et skadedyrproblem:

<img src="/images/sommer26-3.png"
     width="1000"
     style="display: block; margin: 0 auto"
     alt="Mitt serveringssted med syntetiske data">
     
Slik kan vi unngå å se på skadedyrproblematikken på Bølla Burger Bodø i
isolasjon, men heller se den i kontektst av den nasjonale kjeden.

Selve framsiden her er gjort ganske fort og gæli. Jeg fikk god hjelp av
Christian og Teodor til å lage en `nøkkelord-statistikk-feed`, og så har vi bare
pakket sammen noen UI-komponenter fra Designsystemet til Mattilsynet.

Til høsten håper jeg å få hjelp av designerne på teamet til å finne ut hvilke behov
inspektørene har på endepunktet knyttet til en slik statistikk. 
Men før den tid har jeg sommerferie og masterimmatrikuleringsuke.

Jeg har fortsatt masse å lære, både på fremsiden og baksiden, og gleder meg
skikkelig masse til fortsettelsen.

Takk for nå gjengen, 
vi sees på den andre siden av sommeren!
