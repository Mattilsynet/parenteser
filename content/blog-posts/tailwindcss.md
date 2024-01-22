:page/title En uke med Tailwind CSS
:blog-post/published #time/ldt "2024-01-23T09:00:00"
:blog-post/author {:person/id :person/christian}
:blog-post/tags [:framsideutvikling :css]
:blog-post/description

Bedre sent enn aldri sa vi, og tok i bruk Tailwind CSS for første gang. Der fant
vi noen gode idéer, noen dårlige idéer, og en overraskelse eller to.

:open-graph/description

Noen førsteinntrykk etter å ha brukt tailwind CSS i et prosjekt for første gang.

:blog-post/body

La oss bare få det ut av veien først som sist: Grattulerer! Velkommen til
festen, osv. Ja, [Tailwind CSS](https://tailwindcss.com/) er gammelt, men
fremtiden er som kjent [ujevnt
distribuert](https://quoteinvestigator.com/2012/01/24/future-has-arrived/).

I dag har vi lansert en [statisk side](/lange-flate-filer/) for å avløse en
eldre tjeneste, [smilefjesplakater på nett](https://smilefjes.mattilsynet.no/).
Siden [løsningen](https://github.com/mattilsynet/smilefjes-deux) er liten så vi
det som en god anledning til å ta en liten teknisk risiko for å lære litt. Vi
bestemte oss derfor for å bygge grensesnittet med Tailwind CSS, selvom ingen av
oss har brukt det før.

## Hva er Tailwind?

Tailwind er et CSS-rammeverk. Ideellt sett skriver du ingen CSS selv, men bruker
heller Tailwinds [utility classes](https://tailwindcss.com/docs/installation),
så som `py-8` (8 "enheter" padding i y-retningen, altså topp og bunn),
`hover:text-bold` (bold tekst på hover), `flex` (display flex), osv. Det er
_nesten_ inline CSS, men med klassenavn -- noe som gir deg tilgang til media
queries, pseudo-klasser osv. I tillegg til et enormt bibliotek av slike klasser
har Tailwind et byggesystem som gir deg en CSS-fil med kun det du bruker.

[Nicole Sullivans
OOCSS](https://www.stubbornella.org/2009/03/23/object-oriented-css-video-on-ydn/)
lærte meg å elske utility-klasser for over 10 år siden, så å slippe å skrive dem
selv hele tiden var et attraktivt innsalg.

## Det gode

Tailwind sjarmerte fra første øyeblikk. Selv om jeg ba den følge med på noen
Clojure-filer (som jeg tvilte på at den ville forstå) har bygget ikke skuffet
meg én eneste gang -- det gjør alltid det jeg vil, og mere til, det er raskt, og
etter at du har starta det tenker du ikke noe mer på det. 👍

Tailwind har et system for å generere utilities med og uten forskjellige
modifikatorer, et svært effektivt verktøy for å bygge layout. Jeg gjorde nylig
[en mobiltilpasning av en
side](https://github.com/Mattilsynet/smilefjes-deux/commit/d3e034653de894d2a07d64d2af5ddb0b50212187)
som godt illustrerer denne styrken.

Vi startet med denne markupen (uttrykt med Clojure-datastrukturer, såkalt
[hiccup](https://github.com/weavejester/hiccup)):

```clj
[:div.flex.px-8.py-4
 [:div.max-w-72.py-4
  [:h2.text-lg.font-medium.mb-4
   "Alle smilefjes på ett sted"]
  [:p.mb-4
   "Et smilefjes fra Mattilsynet viser hvordan
    vi vurderer viktige forhold som hygiene,
    rengjøring og vedlikehold på et spisested."]
  (icon-button
   {:icon icons/smilefjes
    :href "https://www.mattilsynet.no/mat-og-drikke/forbrukere/smilefjesordningen"}
   "Les mer om smilefjes")]
 [:div.max-w-full.py-4.w-full
  [:div.md:pl-16 (svg "/images/inspektør.svg")]]]
```

Det viktige her er at den ytre div-en har `flex` for å vise de to div-ene under
ved siden av hverandre:

<img class="img" style="max-width: 100%; width: 600px; margin: 0 auto; display: block" src="/images/inspektor-flex.png">

Dette fungerer ikke så godt på små skjermer. Jeg trengte å droppe flex på små
skjermer og redistribuere paddingen litt slik at ting ikke havna klint oppi
hverandre. Og det gjorde jeg på følgende vis:

- `flex` på containeren ble til `md:flex`, som betyr: flex fra og med det
  [forhåndsdefinerte
  breakpointet](https://tailwindcss.com/docs/responsive-design) `md`.
- `p-8` på containeren ble byttet ut med `px-8` og `py-4`. Da har den samme
  horisontale padding, men bare halvparten av den vertikale.
- `py-4` på de to under-elementene, som gir samme spacing på stor skjerm, men
  også passe vertikal padding mellom elementene på små skjermer.
- `md:pl-16` på svg-illustrasjonen, slik at den har padding til venstre når den
  står side-om-side med teksten.

Hele jobben tok meg fem minutter og var en sann glede. Alle mulige utilities
"finnes" allerede, det er bare ta dem i bruk. Tailwind sørger for at CSS-fila
kun har de som er i bruk. Resultatet:

<img class="img" style="max-width: 260px; margin: 0 auto; display: block" src="/images/inspektor-mobil.png">

Som du ser antydning til over, så har Tailwind et veldig fint system for
[spacing](https://tailwindcss.com/docs/customizing-spacing), og tilsvarende også
for [farger](https://tailwindcss.com/docs/customizing-colors).

## Det dårlige

Siden byggeklossene til Tailwind er såpass atomiske blir det fort mange av dem
når du skal lage mer komplekse greier. Se bare på dette eksempelet -- kan du se
hva det er?

```clj
[:a
 {:href "https://www.mattilsynet.no/mat-og-drikke/forbrukere/smilefjesordningen"
  :class ["flex" "items-center" "border" "rounded"
          "px-4" "py-2" "font-medium" "border-granskog-800"
          "border-2" "text-granskog-800"]}
 [:div.w-4.mr-2 icons/smilefjes]
 "Les mer om smilefjes"]
```

Det er en knapp, et poeng som til en viss grad drukner i visuelle detaljer.
Dersom du skal ha to knapper i kodebasen din tvinges du mer eller mindre til å
enten trekke ut dette i en funksjon, eller for alltid ha små forskjeller på de
forskjellige knappene. Å holde denne lista med klasser lik på flere steder blir
fort knøvlete -- og dette er et nokså uskyldig eksempel, hardføre
Tailwind-brukere har sett langt verre.

Jeg nevnte spacing- og fargesystemene over. Fargesystemet er bra, men kanskje
ikke på den måten Tailwind ber deg bruke det. Det legger nemlig opp til at du
kaller en blå spade blå, og bruker klassenavn ala `border-blue-100`. Tenk deg at
du har 1000 sånne klasser i kodebasen din, og så kommer designeren med et nytt
theme. Au.

Tailwind legger opp til at du kan komme med dine egne farger, og problemet over
løses trivielt ved å gi disse mer "semantiske" navn, så du heller kan si
`border-primary-100`.

## Semantikk

Tailwind sier at semantisk CSS ikke skalerer. Og så finnes det
plugins/komponentsystemer til Tailwind, som feks
[DaisyUI](https://daisyui.com/), som selger seg inn på denne måten:

<img class="img" style="max-width: 100%; width: 600px; margin: 0 auto; display: block" src="/images/daisy.png" alt="Instead of writing 100 class
names, for every element, every page, every project, again and again... use
semantic class names">

Når disse to verktøyene tilsynelatende har så motstridende meninger om semantikk
så mistenker jeg at de snakker om forskjellig semantikk. Semantikken Tailwind
vil til livs er nok klassenavn ala `.book-search` og `.shopping-cart` -- altså
CSS-klasser navngitt etter forretningsdomenet ditt. DaisyUI sin semantikk dreier
seg om [UI-komponenter](https://daisyui.com/components/) -- altså knapper, tabs,
og lenker. Å treffe [rett
domene](https://www.kodemaker.no/blogg/2023-01-domenemodell-frontend/) er like
vanskelig som det er viktig, se gjerne [foredraget
mitt](/datadreven-frontend/) for mer om dette.

## Konklusjon

[Som
forventet](https://github.com/Mattilsynet/smilefjes-deux/blob/main/adr/04-css.md)
ble det litt rot av vår første implementasjon med Tailwind. Men vi har lært mye,
og har et tydelig bilde av hvordan vi ønsker å bruke det videre.

Jeg kommer nok ikke til å basere en hel site på kun Tailwind igjen. Det blir
veldig mye detaljer og for lavt abstraksjonsnivå for min smak. Det må kombineres
med et komponentbibliotek -- jeg trenger å kunne kalle en knapp en `btn`.
Personlig har jeg trua på noe som spiller på lag med Tailwind, ala DaisyUI. Det
får bli neste eventyr.
