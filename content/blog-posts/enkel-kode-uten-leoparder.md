:page/title Enkel kode uten leoparder
:blog-post/author {:person/id :person/teodor}
:blog-post/published #time/ldt "2025-10-10T09:00:45.689227"
:blog-post/tags []
:blog-post/description

Det er vanskelig å skrive enkel kode.
Én ting du kan gjøre er å fjerne de kjente leopardene.
For å fjerne leopardene må du forstå fundamentet.

:blog-post/body

![Leopard nr 1](/images/leopard1.jpg)

_Husk å kjøre setup-scriptet før du starter dev-miljøet (krever Ruby 3).
Bilde: [Wikimedia](https://en.wikipedia.org/wiki/File:African_leopard_male_(cropped).jpg), CC S-A 4.0_

Denne uka har jeg fått kode litt sammen med Isam, som startet som utvikler i Mattilsynet på mandag.
Parprogrammeringen har fått meg til å tenke på hva vi har gjort, og hvorfor vi har gjort det.

Vi ble tidlig enig om at å lage en enkel personlig nettside for Isam var et fint mål.
Så, putt "hei" i index.html og gå i prod?

```
$ echo "hei" > index.html
```

Men for å komme oss dit, støtte vi på spørsmål!
Hva er `echo`?
Hva er `echo $PATH`?
Hva gjør `export PATH="/opt/homebrew/bin:$PATH"`?
Hvordan kan vi finne programmer med `which`?
Hva skjer når vi har flere `vim`-programmer på PATH?
Hva er forskjellen på terminalen (for eksempel Terminal eller Ghostty) og skallet (for eksempel Zsh, Bash eller Fish).

![Leopard nr 2](/images/leopard2.jpg)

_Etter du har prodsatt ny versjon, må du SSH-e inn på ingressen og tømme Varnish-cache, hvis ikke må du vente på automatisk cache-tømming (cirka 30 minutter).
Bilde: [Wikimedia](https://en.wikipedia.org/wiki/File:Indochinese_leopard.jpg), CC S-A 4.0_

Vi tok oss god tid til å forklare hver ting: terminal, skall, programmer, variabler i skallet, og hvordan `PATH` er spesiell.

Etter første økt (tirsdag) hadde vi nettside i prod, fra et Git-repo med to filer: `index.html` og `dev`.
`index.html` var første versjon av nettsiden, og `./dev` ga en live-server.

Etter andre økt (torsdag) hadde vi enkel støtte for innlegg på bloggen.
Hvert innlegg er sin egen HTML-fil, og `build.js` samler innleggene og lager `index.html`.

Men koden var full av leoparder!
Da var det [på tide å rydde](/refaktorering/).
Vi fjernet `console.log()` vi hadde skrevet før for å sjekke noe.
Vi fjernet setting av variabler som ikke ble brukt.
Vi fjernet skriving av en urelatert fil.

[Når jeg setter meg ned for å kode, vil jeg føle meg avslappet.](https://px16.matt.is/)
Hvis leoparder spretter opp av hull i bakken, ramler ned fra trær og hopper ut fra klesskapet mitt og biter meg i tåa, blir jeg stressa!

![Leopard nr 3](/images/leopard3.jpg)

_Ikke kall sendRequest() før du har kjørt prepareRequest med de samme argumentene!
Bilde: [Wikimedia](https://en.wikipedia.org/wiki/File:Day_47_Leopard_(Panthera_pardus)_male_marking_a_bush_with_urine_..._(53310719953).jpg), CC S-A 4.0_

Oppsummert:

- For å skrive enkel kode, må du forstå fundamentet: terminalen din, programmeringsspråket ditt, Internett og nettleseren.
- Du kan forenkle koden ved å fjerne leopardene du ser.
- Du får enklere kode ved å løse nye problemer på enklest mulig vis.
  Det er sjelden nyttig med halvferdig kode som planlegger for framtidige eventualiteter.

Takk til Douglas Adams og Jack Rusher for den nydelige leopard-analogien.
Jack Rusher har skrevet [Leopards in the source code](https://jackrusher.com/journal/leopards-in-the-source-code.html),
mens Douglas Adams har denne snutten i _Haikerens Guide til Galaksen_:

> “But the plans were on display…”
> <br>“On display? I eventually had to go down to the cellar to find them.”
> <br>“That’s the display department.”
> <br>“With a flashlight.”
> <br>“Ah, well, the lights had probably gone.”
> <br>“So had the stairs.”
> <br>“But look, you found the notice, didn’t you?”
> <br>“Yes,” said Arthur, “yes I did. It was on display in the bottom of a locked filing cabinet stuck in a disused lavatory with a sign on the door saying ‘Beware of the Leopard.” 
