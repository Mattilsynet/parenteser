:page/title Arkitektens nye klær
:blog-post/author {:person/id :person/christian}
:blog-post/published #time/ldt "2026-04-16T09:00:00"
:blog-post/tags []
:blog-post/description

Hvis AI-agenter er den nye junioren, er vi da de nye løsningsarkitektene?

:blog-post/body

Mange organisasjoner har forsøkt å skalere utviklingen sin ved å skille tenking
og koding. Sett noen få senior løsningsarkitekter til å gjøre det store
tenkearbeidet, og en armé av (billige) juniorer til å skrive koden. Koding er jo
tross alt bare fargelegging av bokser, er det ikke?

Resonnementet er forlokkende: Det er ikke avanserte kodeskills som lager gode
produkter. Det er evnen til å forstå domenet, komme til bunns i hvilke behov
brukerne våre _egentlig_ har, og formulere gode løsninger for dem. Og et skarpt
blikk på avhengigheter og flaskehalser -- i både prosess og systemer -- er det
som hjelper oss å bygge robuste løsninger som brukerne våre blir begeistret for
å bruke. Så hvorfor skal vi da bruke dyre erfarne utviklere til å skrive koden?

Vel, det viser seg at god arkitektur ikke blir til på det nivået som en
systemarkitekt tegner den. Den samme arkitekturtegningen kan fargelegges med noe
kluss, eller med de vakreste nyanser og sjatteringer.

Virkelig god arkitektur oppstår ikke i diagrammer, men drives frem av hundrevis
av små valg i koden.

- Vi unngår å løse samme problem på forskjellige måter
- Vi vet hvilke abstraksjoner som venter på et eksempel eller to til før vi gjør
  litt grundigere design med dem
- Vi innfører nye [håndfaste konsepter](/handfaste-konsepter/) ved behov
- Vi holder kvaliteten oppe over tid ved å [rydde mens vi jobber](/refaktorering/)

Disse beslutningene tas ikke én gang, men kontinuerlig, av folk som står midt i
problemet.

Allikevel står folk nå i kø for å bli løsningsarkitekter med en armé av
kodeagenter som fargelegger bokser.
