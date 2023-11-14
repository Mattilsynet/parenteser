:page/title Putekrig og responsive bilder
:blog-post/author {:person/id :person/magnar}
:blog-post/published #time/ldt "2023-11-14T09:30:00"
:blog-post/tags [:css :html :responsiv-design]
:blog-post/vcard-photo /images/putekrig.jpg
:open-graph/image /images/putekrig.jpg

:open-graph/description

Er det putekrig, eller er det mørkere undertoner i dette tilsynelatende lystige
bildet? Akkurat det har jeg ikke noe svar på, men her får du et fint triks for å
lage skikkelig responsive bilder.

:blog-post/description

Det er mange ting jeg lurer på med dette bildet. Hvorfor var det en pute på
kontoret når vi hadde fagdag? Hvorfor grep jeg tak i den? Er det putekrig jeg
oppfordrer til, eller er det mørkere undertoner i dette tilsynelatende lystige
bildet? Etter denne bloggposten er det heldigvis én mindre ting jeg lurer på:
Hvordan lager man sånne responsive bilder igjen?

:blog-post/body

<img class="floaty-photo" src="/images/putekrig.jpg">
Det er mange ting jeg lurer på med dette bildet. Hvorfor var det en pute på
kontoret når vi hadde fagdag? Hvorfor grep jeg tak i den? Er det putekrig jeg
oppfordrer til, eller er det mørkere undertoner i dette tilsynelatende lystige
bildet? Etter denne bloggposten er det heldigvis én mindre ting jeg lurer på:
Hvordan lager man sånne responsive bilder igjen?

## Problemet

Det er ikke alltid at et svært foto i portrettmodus passer inn (og da tenker
jeg på utformingen, ikke en glisende gærning som skal kvele deg med en pute).
Særlig gjelder det når man skal ta høyde for alt fra små mobiler til den gedigne
skjermen på kontoret.

Kanskje hvis man har god plass så kan man vise hele bildet til venstre, med
teksten på høyre side, i en slik layout:

<div class="responsive-bilder ex-1">
  <div class="bilde faux"></div>
  <div class="ark"><div class="tekst"></div></div>
</div>

Så langt er alt vel.

På en litt smalere skjerm vil kanskje noe sånt fungere?

<div class="responsive-bilder ex-2">
  <div class="bilde faux"></div>
  <div class="ark"><div class="tekst"></div></div>
</div>

På et eller annet tidspunkt må vi nok bytte til å ha bildet over, og da blir jo
dette helt absurd:

<div class="responsive-bilder ex-3">
  <div class="bilde faux"></div>
  <div class="ark"><div class="tekst"></div></div>
</div>

Får være grenser for hvor mye "Magnar med pute" vi skal ha i monitor. Nei, bedre
da å gå for en slik layout:

<div class="responsive-bilder ex-4">
  <div class="bilde faux"></div>
  <div class="ark"><div class="tekst"></div></div>
</div>

Så hva skal en stakkars framsieutvikler gjøre? En ting er sikkert: Vi har ikke
lyst til å lage tre versjoner av bildet.

## På vei mot en løsning

Gode gamle `<img>` kommer til kort her. Vi trenger å kunne justere størrelsen på
utsnittet, uavhengig av selve innholdet i bildet. Det må bli en `<div>`. Så
slenger vi bildet på som `background-image`, og ting blir helt lol:

<div class="responsive-bilder ex-1">
  <div class="bilde putekrig"></div>
  <div class="ark"><div class="tekst"></div></div>
</div>

Okay, det var jo litt dekorativt med den øverste venstre fliken av bildet, men
det var kanskje ikke helt hva vi så for oss.

Se nå hva som skjer når vi slenger på `background-size: cover`:

<div class="responsive-bilder ex-1">
  <div class="bilde putekrig cover"></div>
  <div class="ark"><div class="tekst"></div></div>
</div>

Det er jo fleske meg helt rått.

## Ikke helt rått

Men hva skjer med de to andre størrelsene vi ønsket oss?

<div class="responsive-bilder ex-2">
  <div class="bilde putekrig cover"></div>
  <div class="ark"><div class="tekst"></div></div>
</div>

Hmm...

<div class="responsive-bilder ex-4">
  <div class="bilde putekrig cover"></div>
  <div class="ark"><div class="tekst"></div></div>
</div>

Mjooneeei?

Sitter ikke helt.

## Det siste trikset

For å få denne herligheta helt i mål, så kan vi fortelle nettleseren hvor den
skal fokusere. Og det gjør vi med prosenter og `background-position`:

```
background-image: url(/images/putekrig.jpg);
background-size: cover;
background-position: 50% 25%;
```

<div class="responsive-bilder ex-2">
  <div class="bilde putekrig cover pos"></div>
  <div class="ark"><div class="tekst"></div></div>
</div>

Ser man det, fjeset er jo midt på!

<div class="responsive-bilder ex-4">
  <div class="bilde putekrig cover pos"></div>
  <div class="ark"><div class="tekst"></div></div>
</div>

Og her også!

## Fra bloggpost til prod

For å kunne bruke deilig responsive bilder som dette (nå tenker jeg igjen på
hvordan de legges opp, ikke akkurat dette putebildet), må vi altså ha litt mer
metadata om dem: fokuspunktets posisjon. Det må inn sammen med bildet, enten i
CMSet vi bruker, eller som metadata på filnavnet eller noe slikt.

Og så ender man da opp med noe HTML a-la dette:

```html
<div class="r-img"
     style="background-image: url(...);
            background-position: 50% 25%;">
</div>
```

Det er ikke en søt liten `<img>` lenger akkurat, men så deilig å kunne bruke det
samme bildet i en drøss forskjellige visninger, da gett.

## Oppdatering!

Thomas Østdahl på Team Inspektørhverdag her hos Mattilsynet gjorde meg
oppmerksom på et triks for å kunne bruke `<img>` likevel:

```html
<img src="..." style="object-fit: cover;
                      object-position: 50% 25%;">
```

Her bruker vi litt nyere CSS enn forøvrig i bloggposten, men begge reglene har
bred støtte i alle nettlesere i følge
[caniuse.com](https://caniuse.com/object-fit), så da kan vi lage responsive
bilder med enda bedre samvittighet. Takk, Thomas!

## En siste tanke

Denne teknikken ber deg ta stilling til spørsmålet "Hva er viktig i dette
bildet?" Det samme spørsmålet er jo også høyst relevant når du skal skrive en
god beskrivelse av bildet for å tilgjengeliggjøre innholdet for skjermlesere.
Noe å ta med seg. Mobilvennlig, desktopvennlig, og universelt utformet. Da kan man klappe sammen
lokket på datamaskinen på slutten av dagen med god samvittighet.
