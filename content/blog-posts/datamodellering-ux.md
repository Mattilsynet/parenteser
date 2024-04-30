:page/title Datamodellering er også UX-design
:blog-post/author {:person/id :person/christian}
:blog-post/published #time/ldt "2024-04-30T09:00:00"
:blog-post/tags [:modellering :ux]
:open-graph/image /images/strekkode.png
:blog-post/description

Det er lett å tenke at brukervennlighet er designerne og frontend-folka sitt
ansvar, men mange av valgene vi tar i datamodellen og systemarkitekturen
påvirker også hvor hyggelig (eller ikke) det blir å bruke systemene vi lager. La
oss se litt nærmere på bruk av unike id-er.

:open-graph/description

Noen tanker om hvordan valg vi tar i datamodellen og systemarkitekturen påvirker
brukskvaliteten på systemene vi bygger, med fokus på unike id-er.

:blog-post/body

Det er lett å tenke at brukervennlighet er designerne og frontend-folka sitt
ansvar, men mange av valgene vi tar i datamodellen og systemarkitekturen
påvirker også hvor hyggelig (eller ikke) det blir å bruke systemene vi lager.

Ethvert system trenger å adressere dataene sine med id-er. Disse er viktige både
internt i systemet, men også mot omverdenen. I webapper har id-er en tendens til
å dukke opp i URL-er og andre steder i brukergrensesnittet. I fagsystemer blir
de kanskje til og med en del av vokabulæret til de som bruker dem.

Særlig URL-er er verdt å ofre en tanke. Hvor skal disse figurere? Skal de
trykkes på plakater? Være på TV-reklame? Sendes på epost og/eller SMS? I det
øyeblikket du skal publisere en lenke på Facebook angrer du kanskje litt på at
du valgte UUID-er til å adressere innholdet ditt.

Men hva er en god id da? La oss se på noen alternativer.

## Naturlige id-er

Mange objekter har en såkalt naturlig id. Enheter i Brønnøysundsregisteret har
unike organisasjonsnummer, en kommune har et kommunenummer, en person har et
personnummer og en ku har et individnummer.

Naturlige id-er er dårlige kandidater for adressering, nettopp fordi de ikke
eies og kontrolleres av systemet. Hver gang det er kommunereform i Norge er det
en hel haug med naturlige id-er som endrer seg.

## Numeriske database id-er

Løpende numeriske id-er fra en database er nok et eksempel på id-er som du
strengt tatt ikke kontrollerer, og som nødig bør brukes utenfor systemet ditt.

## UUID-er

UUID-er har blitt veldig populært de siste årene. UUID-er er lette for utviklere
å genere uten noen form tilstand. Når ting er lett for en utvikler så vet du at
det kommer til å dukke opp overalt. Problemet med UUID-er er at de er utrolig
brukerfiendtlige. Bare tenk deg om denne bloggposten bodde på følgende URL:

```
https://parenteser.mattilsynet.io/blogg/58ebdbab-27ac-4b77-8056-1d3c6ee87e9b/
```

Se for deg at du skal lese opp denne URL-en til bestefaren din over telefon.
Lykke til!

## Slugs

En "slug" er en URL-tilpasset versjon av et navn eller lignende. Disse har vært
populære som "URL-id-er". Dessverre er de lite brukandes som id-er, da de stort
sett representerer flyktig innhold (et navn som kan endres). Dersom man skal
bruke slugs så bør de genereres kun én gang, og aldri endres.

Bloggposten du nå leser har en håndlaget "slug" som URL. Det passer fint for
håndlaget innhold, men skalerer dårlig som mekanisme for generell adressering.

## System-unike id-er

Et bedre alternativ er å lage id-er som er unike innenfor systemets rammer. Ved
å snevre det inn fra "universally unique" (UUID) til "system unique" trenger vi
ikke like mye entropi og kan slippe unna med langt hyggeligere id-er.

En id kan feks bestå av 6 tegn, en blanding av tall og bokstaver. Med alle
tallene og det norske alfabetet har du da `39^6` id-er å ta av -- i overkant av
35 milliarder. Det skulle dekke de fleste behov.

En id på 6 tegn er åpenbart lettere å sjonglere enn en UUID. Men kan vi gjøre
det bedre? Vel, noen tegn er kjipere å ha med å gjøre enn andre. Se på dette
eksempelet:

> 0l9234

De to første tegnene -- er det `o` eller `0`? `l`, stor `I`, eller `1`? Avhengig
av font og størrelse kan det være veldig vanskelig å se. Vi kan øke lesbarheten
ved å unngå tegn som er vanskelig å skille fra hverandre.

`0`, `1`, `o` og `l` er ute. I tillegg kan vi droppe `æ`, `ø`, og `å`, ettersom
de har det med å lage trøbbel på steder der id-er ferdes. `b`, `d`, `p` og `t`
høres veldig like ut, det samme gjelder `m` og `n`. Vi stryker også `v` og `y`.

Da står vi igjen med et alfabet som ser sånn ut:

```
23456789acefghjkqrsuwxz
```

Lusne 23 tegn, men med kun 6 av dem kan vi fortsatt lage 150 millioner id-er.
Med 7 har du nesten 2 milliarder id-er.

## Gjør det lett å gjøre rett

Du lurer kanskje på hvorfor vi ikke bare kan lage fine URL-er for innhold som
skal eksponeres i grensesnittet?

Ved å sørge for at systemets id-er er leselige og brukervennlige i seg selv har
vi gjort det nærmest umulig for utviklerne å dytte obskure sekvenser med tall og
bokstaver i ansiktet på brukerne.

Det er fortsatt mulig å lage håndsydde slugs på URL-er der det passer seg, men
selvom vi glemmer oss bort så faller vi tilbake på en form for adressering som
er mennesketilpasset.

Utviklerne får også glede av disse ID-ene når vi skal grave i data, følge opp
saker i monitoreringen vår og interagere med databasen. Og brukerne får URL-er
og id-er som de klarer å formidle muntlig og skrive riktig på første forsøk.
Alle vinner.

For de spesielt interesserte har jeg skissert [en konkret implementasjon i et
eget innlegg](/brukervennlige-ider-clojure/).
