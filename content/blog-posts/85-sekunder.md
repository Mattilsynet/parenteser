:page/title Jakten på de forsvunnede 85 sekunder
:blog-post/author {:person/id :person/magnar}
:blog-post/published #time/ldt "2023-12-12T09:00:00"
:blog-post/tags [:observability :ops]
:blog-post/description

Bli med på årets julemysterie, i en desperat jakt på kriminelt lange
responstider.

:blog-post/body

"Magnar, nå hakker serveren igjen." Beskjeden kom tikkende inn på melding fra en
av spillerne av [det tekstbaserte eventyrspillet Adventur Delux](https://www.adventur.no).

Hmmm... *igjen?* Jeg åpnet dashboardet for serveren og så over tallene. Alt så
fint ut. Masse minne tilgjengelig. Null stress på CPU. 95- og 99-persentilene på
responstider under 50 millisekunder.

"Nettverkstrøbbel", konkluderte jeg med. Sendte tilbake: "Sitter du på litt
ustabilt nett, kanskje?" Ja, jo, det kunne tenkes, var svaret. Han satt jo på
mobilen. Supert, da var alt som det skulle.

Dette var et halvt år siden.

Alt var ikke som det skulle.

## En voksende uro

De neste månedene fikk jeg noen små drypp med rare hendelser. Jeg fikk
feilmelding om at en spiller hadde valgt et alternativ som ikke fantes. Hvordan
kunne det skje? Kanskje to requester ramlet inn samtidig på grunn av dårlig
nett? Ikke umulig. Slettes ikke umulig.

Av og til kom en "Nå lugger det på serveren"-rapport fra en spiller. Kanskje en
gang i måneden omtrent? Monitoreringen min avslørte ingen ting. Serveren hadde
lav last og masse minne. Igjen; nettverkstrøbbel.

Men hver lettvinte "nettverkstrøbbel"-konklusjon brakte med seg en nagende
følelse. Så jeg bestemte meg for å eliminere det som unnskyldning. Javisst kunne
dårlig nett forklare problemene, men jeg måtte skaffe bevis.

## Sporing på klienten

Klienten hadde allerede en ganske robust håndtering av feil og timeouts på
requests. Den prøvde flere ganger, ventet litt lenger for hver feil, og
avsluttet med å be spilleren prøve igjen senere.

Problemet mitt var at jeg så historien bare fra serverens ståsted. Jeg så bare
de requestene som faktisk kom frem, og i den rekkefølgen de ramlet inn til
serveren. Historien på klienten kunne være en ganske annen.

Jeg bestemte meg for å lage en pakke av loggmeldinger for hele forsøket --
inkludert alt av requester. Dersom klienten hadde en dårlig opplevelse, sendte
den pakken til serveren ved første anledning, som da igjen fyrte av en e-post til
meg.

Allerede første dag med dette i prod fikk jeg en e-post. Og en ting ble veldig tydelig:

Det var ikke nettverkstrøbbel.

## 85 sekunder

Den første requesten nådde frem til serveren som den skulle, men klienten
gav opp å vente etter en 4 sekunders timeout, og prøvde på nytt.

Serveren svarte: "Vent, jeg holder på."

Klienten ventet litt, og prøvde så igjen.

"Vent, jeg holder på."

Igjen.

"Vent, jeg holder på."

Igjen.

"Vent, jeg holder på."

Igjen. Igjen. Igjen. Igjen. Til klienten ga opp.

Spilleren fikk beskjed om å prøve på nytt igjen litt senere. Klikk! Prøv igjen.

"Vent, jeg holder på", sa serveren.

*Den holdt fortsatt på med første request.*

Faktisk skulle serveren holde på med første request i vanvittige **85
sekunder**. Ikke rart jeg fikk melding om at "serveren lugger."

## Her ær'e noe gæli som ente er rekti

Jeg satt meg ned og prøvde å spille litt selv. Alt var lynraskt, som alle de
andre gangene jeg hadde prøvd. Alle dashboard viste fortsatt blå himmel og godt
under 50 millisekunder på gjennomsnittlige, 95- og 99-persentil responstider.

Jeg SSH-et meg inn på serveren, kjørte `top` og myste skeptisk på tallene. Jeg
er ingen ekspert på å tolke `top`, men jeg syns da det så ganske bra ut. I
desperasjon spurte jeg ChatGPT om å tolke top-headeren:

> *In summary, this system is very stable, with extremely low CPU and memory pressure.*

Ja, det var det jeg også trodde. Fortsatt ikke noe nærmere en løsning, da.

Så hva var spesielt med akkurat denne requesten?

<img class="floaty-photo" src="/images/usual-suspects.jpg">
Jeg tenkte på de mest kostbare operasjonene i spillet: Det er ganske krevende
når eventyreren hopper langt tilbake i tid ... Nei, her var det valgt et helt
vanlig alternativ.

En annen kostbar operasjon er når eventyreren overnatter, for da blir alle
rangeringslistene oppdatert ... Niks, det var ikke det heller.

Jeg gikk gjennom absolutt alt jeg kunne tenke meg. Jeg finleste koden. Jeg
tittet i logger. Til slutt satt jeg igjen med bare én forklaring. Det måtte være
en hinsides lang Garbage Collection.

Jeg oppgraderte til ny versjon av JDK med en bedre GC-algoritme og justerte ned
minnet på serveren, med håp om å ikke få flere e-poster.

> *"Hope... Ha! Such a tease!"*
>
> -- Raphael, Baldur's Gate 3

## Et lite sidespor

<img class="floaty-photo" src="/images/obseng.jpg">

Her på Mattilsynet har vi annenhver uke satt av en halv dag til selvvalgt
kompetanseutvikling. Christian var begeistret for boka [Observability Engineering](https://www.honeycomb.io/wp-content/uploads/2022/05/Honeycomb-OReilly-Book-on-Observability-Engineering.pdf),
så jeg begynte å lese.

De første fire kapitlene handler om hvordan metrics og logger kommer til kort i
kompliserte systemer. At de gir for dårlig innsikt i hva som faktisk foregår på
tvers av prosessene våre. At man ender opp med å måtte lene seg på intuisjon, og
heurestikker basert på tidligere erfaringer med systemet.

Ja, gjett om jeg kjente meg igjen.

Boka foreslår en alternativ tilnærming som baserer seg på *traces* og *spans*,
med mål om å gi det de kaller *observability*: Evnen til å forstå hva som
foregår i systemet, uansett om man aldri har sett situasjonen før, og uansett
hvor bisarr den måtte være.

## Observability

Jeg tror vi kommer til å skrive mer om observability på denne bloggen, men
la meg raskt forklare:

- **Spans** er -- i likhet med loggmeldinger -- små snutter av informasjon om hva som
foregår i en prosess.

Forskjellen er at de er strukturerte data over et tidsutsnitt. De har
`timestamp` og `duration`, og alt av relevant informasjon man ville slengt med
i en loggmelding.

I tillegg har de en `parent` - altså, de er organisert i en trestruktur.

- **Traces** er en samling med slike spans, og representerer én request inn i
systemet, på tvers av klienter, servere, backends, og mikrotjenester.

Observability får du når du samler disse i store nok mengder, og med verktøy som
hjelper deg å finne mønstere i disse store datamengdene. Det var ikke det jeg
gjorde for å finne de 85 sekundene, men boka hadde gitt meg en idé.

## Hjemmesnekra spans

Det var ikke Garbage Collection, skjønte jeg fort. Jeg fikk flere e-poster med
varsel om vanvittige responstider, ny JDK til tross. Likevel var ikke alt håpet svunnet.

Dette med en trestruktur av *spans* istedenfor loggmeldinger ga meg en ny
tilnærming til å finne problemet mitt. Jeg snekret sammen en [`with-span`](https://gist.github.com/magnars/849cdc6e0b6f0cc109f38c912ddc7144) macro i
Clojure, som jeg brukte slik:

```clj
(telemetry/with-span 'update-rankings {:player-id player-id}
  (update-rankings conn player-id))
```

Her gir jeg span'en et navn, slenger på relevante parametere, og så sørger
makroen for at timestamp, duration og trestrukturen blir riktig.

Jeg slang også sammen et lite UI. Her er et eksempel. Du kan se trestrukturen av
spans, og hvor lang tid hver enkelt tok.

<img src="/images/spans1.png" style="max-width: 100%">

Som du ser så vil tiden til foreldre-spans være satt sammen av tiden til sine
barn-spans.

Så ... hvordan så 85-sekunderen ut?

Hold deg fast.

<img src="/images/spans2.png" style="max-width: 100%">

Hva behager?

Tidslinja blir totalt dominert av de 85 sekundene, men det er ingen child spans
som vil vedkjenne seg tiden. ...?

Litt lenger nede ser vi noe mer:

<img src="/images/spans3.png" style="max-width: 100%">

Hva sa du, sa du? *misc*??

Igjen, ingen av barna tar noe tid.

## Så var det altså bare min feil likevel

For det var jo ikke nettverkstrøbbel. Og ikke var det Garbage Collection heller.
Selvfølgelig var det ikke det. De er alt for åpenbare skurker til å være morderen.

Gjemt mellom linjene her lå koden jeg ikke hadde tenkt på. Ikke hadde sett for
meg at kunne være feilen. Aldri ville kommet på av meg selv. *Jeg hadde i så stor
grad sett bort fra den at jeg ikke engang gadd lage en span.*

Hvorfor det?

Fordi det var en feature som jeg hadde skrudd av for tre år siden.

Når jeg slengte en `with-span` rundt den også, så lyste den mot meg med sine
uendelig lange 85 sekunder.

Slik hang det sammen: Sånn omtrent 1 av 25 000 requests klarte kunststykket å
kjøre den gamle koden -- *uten effekt* -- (featuren var jo skrudd av) og hentet
enorme mengder data ut av databasen for å generere ... en nedlagt rangeringsliste.

Hva slags rangeringsliste, spør du?

Joda, det var lista over "Årets fartsfantomer".

Akkurat, ja.
