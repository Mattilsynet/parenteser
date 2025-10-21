:page/title Ekspert på utviklerverktøyet ditt
:blog-post/published #time/ldt "2025-10-21T10:00:00"
:blog-post/author {:person/id :person/mathias}
:blog-post/tags [:utviklerverktøy :emacs]
:open-graph/image /images/grinch.jpeg
:blog-post/description

Hvordan det er når du har utviklerverktøyet ditt sydd rett på fingerspissene.

:blog-post/body

VSCode, IntelliJ, Sublime, Vim, Emacs - verktøy vi utviklere bruker hver eneste
dag. Men hvor godt kjenner du utviklingsverktøyet ditt? Og går du aktivt inn for
å bli bedre på det? La oss se litt på hva vi kan få til når vi bruker litt tid
på å bli fortrolig med verktøyet vårt!

Jeg har brukt Emacs med [konfigen til
Magnar](https://github.com/magnars/emacsd-reboot) på fulltid i over 9 måneder
nå. Det gikk sakte og var veldig slitsomt i starten – læringskurven er steil.
Men gradvis, litt etter litt, som månedene gikk, ble jeg bedre og bedre. Jeg
lærte meg mer og mer, flere og flere kommandoer satt seg og jeg følte meg mer og
mer kyndig. Jeg lagde til og med mine egne keybindings og kommandoer for å bedre
flyten enda mer.

Som dere nok allerede vet bruker vi Clojure her på Team Servering. Og med Clojure
har vi REPL-et, som vi bruker hyppig hver dag til interaktiv programmering. Her
kommer et utsnitt av de kommandoene jeg bruker oftest i forbindelse med nevnte
REPL:

<small>(Stor `C` er CTRL. Stor `M` er CMD.)</small>

**`C-x C-e`**

Evaluerer et uttrykk (en form) i Clojure:

<video autoplay loop muted playsinline loading="lazy"
       disablepictureinpicture="true" disableremoteplayback="true">
    <source src="/videos/C-x-C-e.mp4" type="video/mp4">
</video>

**`C-c C-c`**

Fungerer på samme måte som `C-x C-e`, men musepekeren kan stå hvor som helst
inni formen:

<video autoplay loop muted playsinline loading="lazy"
       disablepictureinpicture="true" disableremoteplayback="true">
    <source src="/videos/C-c-C-c.mp4" type="video/mp4">
</video>

**`C-c C-p`**

Evaluerer formen og pretty printer resultatet i et annet vindu:

<video autoplay loop muted playsinline loading="lazy"
       disablepictureinpicture="true" disableremoteplayback="true">
    <source src="/videos/C-c-C-p.mp4" type="video/mp4">
</video>

**`C-c C-f`**

Igjen, denne fungerer lik `C-c C-p`, men musepekeren kan stå hvor som helst inni
formen:

<video autoplay loop muted playsinline loading="lazy"
       disablepictureinpicture="true" disableremoteplayback="true">
    <source src="/videos/C-c-C-f.mp4" type="video/mp4">
</video>

**`C-c M-w`**

Denne er det jeg som har lagt til. Den lar deg evaluere formen og kopierer
resultatet til utklippstavlen:

<video autoplay loop muted playsinline loading="lazy"
       disablepictureinpicture="true" disableremoteplayback="true">
    <source src="/videos/C-c-M-w.mp4" type="video/mp4">
</video>

**`C-c C-M-w`**

Denne gjør det samme som `C-c M-w`, men musepekeren kan stå hvor som helst:

<video autoplay loop muted playsinline loading="lazy"
       disablepictureinpicture="true" disableremoteplayback="true">
    <source src="/videos/C-c-C-M-w.mp4" type="video/mp4">
</video>

**`C-c C-M-s`**

Enda en som jeg har lagd. Denne pretty printer en verdi som er `def`-et opp
(musepekeren kan stå hvor som helst inni formen). <small>_Vanligvis hadde du
måtte stå rett på symbolet `bruker-med-team` eller rett etter og bruke `C-c
C-p`._</small>

<video autoplay loop muted playsinline loading="lazy"
       disablepictureinpicture="true" disableremoteplayback="true">
    <source src="/videos/C-c-C-M-s.mp4" type="video/mp4">
</video>

Du tenker kanskje:

> Men hvorfor trenger du alle disse, veldig like, kommandoene? Kunne du ikke
> bare nøye deg med to eller tre?

Jo, det kunne jeg. Men ved å ha alle disse veldig spesifikke kommandoene for
hånden, gir det meg en finfølelse uten like. Jeg får til å gjøre akkurat det jeg
vil, med én gang, uten å tenke så mye på det, uten å tenke på hvor musen står.
Jeg danner meg et synergisk forhold til Emacs.

Poenget jeg ønsker å trekke fram er ikke å heie på Emacs. Du vil kunne få til
det samme i Vim eller VSCode eller hva du enn måtte bruke. Rettere, det jeg vil
med denne artiklen, er å vise effekten av å kjenne utviklerverktøyet sitt fult
ut, eller nærmere effekten av å ha brukt litt tid på å bli dus med verktøyet
sitt – hvordan det er å ha verktøyet sitt sydd rett på fingerspissene.

<img src="/images/grinch.jpeg" alt="Grinchen-som-syr-stoff-på-fingrene-sine"
     style="width: 70%; margin: 0 auto; display: block; border-radius: 4rem;"/>

## Oppsummert

- Bruk litt tid på verktøyet ditt.
- Ikke ta alt på én gang, men lær deg litt hver dag. Plutselig kan du mye.
- Finn ut hvordan du legger til egne keybindings.
