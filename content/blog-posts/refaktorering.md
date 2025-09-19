:page/title Rydd mens du jobber
:blog-post/author {:person/id :person/christian}
:blog-post/published #time/ldt "2025-09-19T09:00:00"
:blog-post/tags [:produktivitet :samarbeid :metodikk]
:blog-post/description

Kode blir rotete. Derfor må vi rydde mens vi jobber, ikke spare opp rotet til
ukeslange refaktoreringssprinter.

:blog-post/body

Kode blir rotete. Derfor må vi rydde mens vi jobber, ikke spare opp rotet til
ukeslange refaktoreringssprinter.

Du vil ikke ha en refaktoreringssprint. Altså, hvis du ikke har refaktorert på
lenge trenger du kanskje en «extreme deep clean». Men det beste er å aldri havne
i en situasjon der du må stoppe alt for å betale ned teknisk gjeld.

Hvordan unngår vi refaktoreringssprinter? Refaktorering er en essensiell del av
å programmere. Små problemer dukker opp hele tiden, og det er en del av jobben å
fikse dem når vi ser dem — for å unngå at de små problemene blir store
problemer. Hvis vi spør om lov til å gjøre denne delen av jobben, har vi
allerede tapt. Det finnes ikke en eneste lege på et eneste sykehus i dette
landet som på noe tidspunkt har spurt sjefen sin om lov til å vaske hendene.

Den viktigste kontinuerlige refaktoreringen er den som gjør plass for ny
funksjonalitet. Det kan være å gjøre eksisterende kode litt mer generell, eller
å flytte kode fra en spesifikk implementasjon nærmere systemets kjerne.

Refaktoreringer av denne typen tar som regel ikke mer tid enn å skrive koden du
trenger på siden av alt. Ofte kan det til og med være tidsbesparende — samtidig
som kvaliteten på kodebasen i helhet blir bedre. Det er en ganske bra deal.

Kent Beck [oppsummerer dette
elegant](https://x.com/KentBeck/status/250733358307500032?lang=en&utm_source=chatgpt.com):

> For each desired change, make the change easy (warning: this may be hard),
> then make the easy change

Dette er selve kjernen i budskapet i [Tidy
First?](https://www.oreilly.com/library/view/tidy-first/9781098151232/)

Ikke alle refaktoreringer er små og raske. Men dersom de er på kritisk linje
skal vi likevel ta oss tiden til å gjøre dem. Min neste oppgave er å endre litt
på datamodellen vår. Jeg skal bruke hele morgendagen på det, og jeg vet ikke om
jeg blir ferdig. Men vi har ikke spurt noen om lov til å gjøre det, for det er
vår faglige vurdering at å bygge videre på det vi har vil koste oss mer enn en
dag eller to i løpet av kort tid.

Gi slipp på refaktoreringssprintene, slutt å spørre om lov til å refaktorere, og
adopter speiderregelen: etterlat kode i bedre stand enn du fant den.
