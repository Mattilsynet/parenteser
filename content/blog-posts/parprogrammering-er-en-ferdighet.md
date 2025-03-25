:page/title Parprogrammering er en ferdighet
:blog-post/author {:person/id :person/teodor}
:blog-post/published #time/ldt "2025-03-25T11:00:00"
:blog-post/tags [:samarbeid :metodikk :parprogrammering]
:blog-post/description

...og som alle andre ferdigheter kommer du ikke til å mestre den første gang du
prøver. Det må trenes opp.

:blog-post/body

Når du først prøver parprogrammering er det noen utfordringer du kan støte på.
La oss se på noen av de vanligste.

## Vanlig starttrøbbel

Parprogrammering bygger delt kontekst -- men krever også delt kontekst. Hvis to
personer til vanlig jobber med forskjellige systemer bygget i forskjellig
teknologi, krever kontekstbyttingen energi.

Parprogrammering funker bedre når koden er eksplisitt og allerede bærer
intensjon. Hvis det i utgangspunktet er trått for én person å sette seg inn i
koden, vil parprogrammeringen "lugge" mer i starten. Den som allerede kjenner
koden vil oppleve at man bare "famler rundt", og den som ikke kjenner koden vil
føle seg retningsvill og føle at de ikke får bidratt.

Parprogrammering krever ledelse. Minst én person må ha en idé om hva man skal få
til og hvordan man kan gå fram.

Den som leder må aktivt inkludere den som følger: "her, nå er det din tur". Og
nyansene her er krevende: man må spille ball _på rett tidspunkt_. Tenk at du
skal spille partneren din god: gi dem tastaturet når de vet hva neste steg er.

Parprogrammering krever et lass av tillit. Man må tåle å tryne skikkelig mens
andre ser på. Én kjip kommentar kan gjøre at man ikke ønsker å forsøke igjen.

## En liten historie om en økt med starttrøbbel

Parprogrammering krever en viss grad av interaktivitet fra systemene man jobber
med. La meg illustrere med en liten anekdote.

Jeg var med på en trå økt parprogrammering en gang. Vi var TI PERSONER (veldig
mange) og skulle rullere. Oppgaven var å rydde opp i en bit kode. Teknologien
var Node / Typescript.

Jeg startet med å sette opp en svært primitiv testkjører - en funksjon og noen
asserts. Ideen min var å få satt opp _en form for test_, så vi kunne få et
begrep om hva koden faktisk skulle gjøre uten å gjette.

Personen etter meg slettet det jeg hadde startet på i testene, og begynte å
endre koden rått (uten å kjøre koden). Så begynte personen etter det å legge til
typedefinisjoner.

Etter to timer hadde vi ikke endret koden på noe meningsfylt vis - vi hadde kun
lagt på noen typedefinisjoner. Vi hadde heller ikke kommunisert med personen som
faktisk hadde tatt fram koden, alle bare skrev for seg selv. Denne situasjonen
er ... det mye man kan si om.

Hovedproblemet i vårt tilfelle var at vi starta alt for vanskelig. Det hadde
vært enklere å starte med to personer: én som kan koden og én som har
parprogrammert før.

Jeg gjentar: parporgrammering er en ferdighet man må bygge opp!
