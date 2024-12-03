:page/title Lynraske modultester med én tastekombinasjon
:blog-post/author {:person/id :person/teodor}
:blog-post/published #time/ldt "2024-12-03T12:39:00"
:blog-post/tags [:metodikk]
:blog-post/description

Å oppleve lynraske modultester i bruk har endret på hvordan jeg liker å strukturere og jobbe med kode.
Kanskje lynraske modultester er noe for deg også?

:open-graph/description

Å oppleve lynraske modultester i bruk har endret på hvordan jeg liker å strukturere og jobbe med kode.
Kanskje lynraske modultester er noe for deg også?

:blog-post/body

Hvordan skriver du koden din?

Lynraske modultester med én tastekombinasion er en fryd å jobbe med fordi det gir en super boost til feedbacken du får når du skriver kode.
Kanskje noe å vurdere hvis du ikke har prøvd?
Les videre for å høre hvorfor du bør ta lynraske modultester seriøst, og hvordan du får det til med Clojure, Javascript, Go og Python.

## Hvorfor du bør ta lynraske modultester seriøst

Du kan kode raskere hvis du kan sjekke om koden funker raskere.
Hvis du _vet_ nøyaktig hva koden gjør, er du klar til å endre den.

Med lynraske modultester får du bekreftet at koden gjør hva du tror den gjør
umiddelbart.
Det lar deg endre og legge til funksjonalitet uten frykt.
Når du også stoler på at testene er korrekte og dekker det du bryr deg om, kan
du også sjøsette ny kode straks den er skrevet og testene er grønne.

Så, hva er god nok feedback fra koden din?
Jeg vil beskrive feedback langs tre akser.

- **Forsinkelse**.
  Hvor lenge må du vente?
  Ti millisekunder? Ti sekunder?
  Hvis du kan holde feedback-loopen din lynrask er det helt supert!
  Her bryr jeg meg om "opplevd lynraskt".
  En 60 Hz-skjerm gir deg et nytt bilde hvert 16. millisekund.
  16 millisekunder er lynraskt!
  200 millisekunder er OK.
  1 sekund er dårlig.

- **Bredde**
  Hvor mye dekker feedbacken du får?
  Når testene er grønne, stoler du nok på testene til å gå rett i produksjon med
  koden?
  Når du lener deg på modultester i arbeidet du gjør, må du kunne stole på at
  modultestene dekker det du bryr deg om i modulen.

- **Ergonomi**.
  Å scrolle gjennom tusenvis av linjer printet i et svart vindu med hvit tekst er dårlig for hodet ditt.
  Se for deg at du kjører en bil, men i stedet for å se veien foran deg, får du
  en tekstkonsoll med linjer skrevet ut av en kjøreassistent.
  Det er null sjanse for at jeg setter meg i passasjersetet i den bilen.

Lynraske modultester er bedre målt langs feedback-forsinkelse, feedback-bredde
og feedback-ergonomi enn andre mekanismer for feedback fra kode jeg har prøvd
før[¹].

## Modultester med én tastaturkombo i Clojure

I Clojure kaller vi modulene våre for _navnerom_.
Hvert navnerom bor i hver sin fil.
Vi legger oppførselen til modulen i én fil, og modultestene i et annen fil.

Lag deg en tastaturkombo som gjør følgende:

1. Lagre filen du har åpen.
2. Re-evaluer filen du har åpen.
3. Kjør modultestene, og gi deg selv et sammendrag.
   (Dette må fungere både når du har markøren din i filen med oppførselen til
   koden, og når du har markøren din i filen med testene)

Ferdig!
Nå har du det!

Hvis du vil prøve Emacs-oppsettet vårt, har [magnars/emacsd-reboot] denne tastaturkomboen bygget inn som `C-c C-k`.
Andre Emacs-oppsett kan prøve `M-x cider-test-run-ns-tests`, og for Calva med VSCode kan man sjekke dokumentasjonen for [Calva Test Runner].
Calva / VSCode har også en helt nydelig støtte for å kjøre flere tastatursnarveier etter hverandre med den fine kommandoen `runCommands`.
Les mer fra Peter Strömberg (skaperen av Calva) i [VS Code runCommands for multi-commands keyboard shortcuts].

[magnars/emacsd-reboot]: https://github.com/magnars/emacsd-reboot
[Calva Test Runner]: https://calva.io/test-runner
[VS Code runCommands for multi-commands keyboard shortcuts]: https://blog.agical.se/en/posts/vs-code-runcommands-for-multi-commands-keyboard-shortcuts/

Jeg anerkjenner at å skrive lynraske modultester kan være en utfordring.
Det kan til og med hende at du må tenke på hvordan du kan få testene til å bli
raske når du deler systemet ditt inn i moduler.

## Modultester med én tastaturkombo i Go og Javascript

Go og Javascript er godt egnet for en kjapp test-loop fordi det er raskt å kjøre
en fil.
At det er kjapt å kjøre en ny fil kan være et godt alternativ til et
kjøretidsmiljø der du kan laste ny kode.

Lag deg en tastaturkombo som gjør følgende:

1. Lagre filen du har åpen.
2. Kjør modultestene til filen du har åpen, og gi deg selv et sammendrag.

Når vi ikke bruker et dynamisk kjøretidsmiljø, slipper vi å tenke på hvilken nye
kode som skal lastes inn.

## Modultester med én tastaturkombo i Python

Python har fått sitt eget avsnitt, fordi etter min erfaring, kan Python-tolkeren
ta litt tid når du importerer tunge biblioteker.

Python har imidlertid et dynamisk kjøretidsimiljø!
Kanskje du er uenig, eller aldri har hørt om det før?
I så fall, sjekk [importlib]!

[importlib]: https://docs.python.org/3/library/importlib.html

```python
>>> import importlib
>>> help(importlib)
```

Se!
Standardbiblioteket kommer med en modul laget for å laste ny kode!
Hvis du fremdeles er på Python 2, kan du se etter [`reload`] (som ikke krever
import av noen moduler).

[`reload`]: https://docs.python.org/2.7/library/functions.html#reload

Så lager du deg en tastaturkombinasjon som gjør følgende:

1. Lagrer filen du har åpen.
2. Laster ny kode fra filen med `importlib`.
3. Kjører testene for den nylig importerte modulen.

## Gjør det!

Koding skal være gøy!
Hvis du rigger deg til med solid oppsett for testing, kan du fokusere på hva du
vil at koden din skal gjøre, i stedet for å bruke dagen på å pønske på hvorfor
koden tryner.

## Appendix A: moro med dynamisk lasting av ny kode i Python

I 2017 og 2018 jobbet jeg med [styrkeanalyse av en bru] som [kanskje i framtiden]
kommer til å krysse Bjørnafjorden.
Bjørnafjorden ligger omtrendt midt mellom Bergen og Haugesund.
Verktøyet jeg brukte til modellering er [Abaqus].
Første versjon av Abaqus kom i 1978: da kunne man skrive 3D-modellen sin som tekst i en inputfil, og få resultater.
Alt implementert i Fortran!

[styrkeanalyse av en bru]: https://www.vegvesen.no/globalassets/vegprosjekter/utbygging/e39stordos/vedlegg/sbj-30-c3-nor-90-re-100-summary-report-rev-0.pdf?v=499082
[kanskje i framtiden]: https://www.vegvesen.no/vegprosjekter/europaveg/e39stordos/fjordkryssing-bjornafjorden/

Dagens Abaqus har både GUI og innebygget Python-miljø.
Det muliggjør mer fancy 3D-modellering enn man kunne før.
Vi regnet på skipsstøt i Abaqus, og simulerte storm med [3DFloat].
For å sørge for at ting stemte mellom modellene, hadde vi Excel-ark og
JSON-filer utenfor som beskrev parameterne til modellen.
Jeg skrev koden for å bygge opp Abaqus-modellen.

I de første iterasjonene av koden, restartet jeg Abaqus for å kjøre koden min på
nytt.
Det gikk jeg etter hvert lei av, det var mye venting for å se hva én endring av
én linje kode førte til.

Så jeg droppet omstart av Abaqus, og sørget heller for at jeg kunne laste ny
kode fra inni Abaqus uten å starte alt på nytt.
Da gikk alt drastisk mye raskere enn før.

Jeg fikk mulighet til å open-source en bit av arbeidet med å laste ny
Python-kode dynamisk, nå tilgjengelig på [github.com/teodorlu/hotload].

Du kan ikke bruke Hotload til å gjøre akkurat hva jeg beskriver her for å få til
lynraske modultester, fordi hotload er laget til å lytte på filer, og kjøre kode med effekter på nytt.
Det var uansett en spennende opplevelse for meg å se at jeg fikk til å ta
kontroll over eget utviklingsmiljø, og at å laste ny kode inn i en kjørende
Python-prosess var helt gjennomførbart.
Hotload er noe du kanskje kunne skrevet til deg selv, koden er én fil på 300 linjer.

[3DFloat]: https://ife.no/en/service/3dfloat/
[Abaqus]: https://en.wikipedia.org/wiki/Abaqus
[github.com/teodorlu/hotload]: https://github.com/teodorlu/hotload

[¹]: Unntatt tabeller og grafer når man jobber med svære datasett, men da
     tenker jeg gjerne på det jeg gjør som utforskning og analyse av data mer enn skriving
     av kode.
