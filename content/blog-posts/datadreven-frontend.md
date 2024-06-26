:page/title Datadreven frontend
:blog-post/published #time/ldt "2024-01-17T09:00:00"
:blog-post/author {:person/id :person/christian}
:blog-post/tags [:framsideutvikling]
:blog-post/series {:series/id :foredrag}
:blog-post/description

Helt siden jeg tok i bruk React for over 10 år siden har min tilnærming til
frontendutvikling blitt stadig mer datadrevet. På JavaZone viste jeg frem litt
hva det har gitt meg, og hvorfor du også bør vurdere en lignende tilnærming.

:open-graph/description

Bedre kode med datadrevne frontender. Hvordan? Hvorfor?

:blog-post/body

I dette foredraget viser jeg frem hvor jeg har havnet etter en 10 år lang reise
der mer datadreven frontendarkitektur har vært målet. Jeg presenterer flere
eksempler fra en kodebase som selv etter 10.000 commits over 9 år er i sitt livs
beste stand: endringer tas på stående fot og features går ut raskere enn noen
gang -- takket være en datadrevet arkitektur.

Foredraget tar deg gjennom reisen og valgene som ble gjort underveis, og alle
fruktene vi høstet underveis. Velbekomme!

<div class="video-responsive">
  <iframe class="video-responsive-item" src="https://player.vimeo.com/video/861600197?h=0084e31028&color=ff9933&portrait=0" allow="autoplay; fullscreen; picture-in-picture" allowfullscreen></iframe>
</div>

<br><br>

## Verktøyene

I løpet av presentasjonen viser jeg frem en del verktøy vi har laget og brukt
for å understøtte prosessen. Her er de viktigste, for de som vil dykke litt
dypere:

- [Portfolio](https://github.com/cjohansen/portfolio) lar deg jobbe isolert med
  UI-komponenter for å fokusere på de visuelle aspektene, litt som Storybook
  gjør.
- [Dumdom](https://github.com/cjohansen/dumdom) ligner på React, men støtter kun
  props, og tvinger deg dermed i en datadrevet retning.
- [m1p](https://github.com/cjohansen/m1p) er et bibliotek som løser blant annet
  i18n på en helt datadrevet måte.

Av disse var det særlig Dumdom som åpnet dørene for mange av løsningene våre.
Det var feks der vi åpnet opp for å kunne ha helt [datadrevne
event-handlere](https://www.kodemaker.no/blogg/2021-11-mer-mindre/).

Dumdom baserer seg på [snabbdom](https://github.com/snabbdom/snabbdom). Etter å
ha brukt det i noen år har jeg innsett at rendering-biblioteket kan -- eller
kanskje bør -- være enda enklere, og jeg har derfor startet på [et nytt
"virtuell DOM"-bibliotek](https://github.com/cjohansen/replicant) som er helt
skjært til beinet. Mer om dette i nær fremtid.

## Videre lesning

En av de viktige poengene i foredraget er at man må ha tydelig for seg hvilket
domene man opererer i. Dette har jeg også [skrevet om
tidligere](https://www.kodemaker.no/blogg/2023-01-domenemodell-frontend/).

Min kollega Magnar har også skrevet om enklere frontendarkitektur ved flere
anledninger. Jeg vil spesielt trekke frem [En enkel frontendarkitektur som
funker](https://www.kodemaker.no/blogg/2020-01-enkel-arkitektur/) og hans
foredrag [Strøm data til nettleseren uten å lage det på nytt hver
gang](https://vimeo.com/289851906), som zoomer enda litt lenger ut på denne
arkitekturen.

<div class="video-responsive">
  <iframe class="video-responsive-item" src="https://player.vimeo.com/video/289851906?h=13141ed614&color=ff9933&portrait=0" allow="autoplay; fullscreen; picture-in-picture" allowfullscreen></iframe>
</div>

Til siste vil jeg anbefale [Dumme
klienter](https://www.kodemaker.no/blogg/2023-02-22-dumme-klienter/), et innlegg
som tar for seg den ekstremt datadrevne tilnærmingen vi tok for å lage et
nettspill i vår screencast
[ZombieCLJ](https://www.kodemaker.no/blogg/2023-02-22-dumme-klienter/). Her
laget vi et helt spill, og det er kun ~230 linjer
[frontendkode](https://github.com/magnars/zombieclj-s2/blob/master/src/zombieclj/client.cljs),
hvorav over halvparten er rene [HTML-komponenter](https://github.com/magnars/zombieclj-s2/blob/master/src/zombieclj/components.cljs).
