:page/title Forsvar mot svartekun... parametere!
:blog-post/author {:person/id :person/magnar}
:blog-post/published #time/ldt "2024-10-01T09:00:00"
:blog-post/tags [:arkitektur]
:blog-post/description

Det er lett å bli for defensiv når man tar imot et parameter. Er denne
`bruker-id`-en en streng, som den burde, eller har jeg fått noe annet? Hvordan
skal jeg forsvare meg mot alt som kan gå galt?

:blog-post/body

Det er lett å bli for defensiv når man tar imot et parameter. Her om dagen skrev jeg
noe slikt:

```clj
(defn hent-bruker [db bruker-id]
  (when (string? bruker-id)
    (let [bruker (d/entity db [:bruker/id bruker-id])]
      (when bruker
        ;; ...
        ))))
```

Er denne `bruker-id`-en en streng, som den burde, eller har
jeg fått noe annet? Kanskje den er `nil`? Kanskje det er en streng, men ikke
faktisk en eksisterende bruker-id? Kanskje har jeg fått Shakespeare's samlede
verker i stedet?

Før jeg vet ordet av det har jeg en hel trappeoppgang av innrykk uten å ha gjort
noe som helst av verdi. Jeg så på det jeg hadde skrevet, og vel, det var ikke
godt.

Jeg kunne høre en sånn record scratch lyd i hodet: *Stopp en hal!*

For denne typen kode er bare støy. Jeg har ikke lyst til å strø om meg med
defensiv kode. Kodens faktiske oppgave blir surret inn i et kratt av frykt,
usikkerhet og tvil.

## Grensevakt

Trikset er å erstatte mylderet av defensiv kode med grensevakter. Ugyldige
parametere må stoppes på kantene av systemet.

Vi har tre kilder til funksjonsparametere. Slik beskytter vi oss:

- **Mot brukerne våre:** Vi validerer og transformerer parameterne ved første
  anledning. Den ytterste koden som tar i mot disse sender dem aldri videre
  uvalidert.

- **Mot integrasjonene våre:** Også her validering og transformering ved første
  anledning.

- **Mot koden vår forøvrig:** Ingen validering. Ingen defensiv kode. Vi stoler
  på at parameterne er riktige.

Legg merke til det siste punktet der. *Vi tillater oss å anta at valideringer
allerede er håndtert.* Det er nesten en sånn tillitsøvelse hvor man skal la seg
ramle bakover, og bli fakket av de som står bak. Det er litt skummelt, men det
er ikke ofte noen deiser i bakken.

## Velkjente parameternavn

Denne grensevakten bør helst være generell. Altså, fungere på tvers av
endepunkter og integrasjoner. Det er ikke noe poeng i bare å flytte den samme
defensive koden til et litt annet sted i stacken. Vi må lage oss litt maskineri
på toppen.

Trikset er såkalte velkjente (*well-known*) parameternavn. Vi gir parametere som
`bruker-id` spesialbehandling i maskineriet. På vei inn gjennom grensekontrollen
vil det valideres både til å være en streng og til å være en konkret bruker i
databasen.

Så lenge man benytter parameternavn som grensevaktene kjenner til, så trenger
man ikke selv å sjekke noe mer. Og hvis du trenger et nytt navn, så
registrerer du samtidig hvordan det skal valideres og transformeres.

## Transformering

Hva mener jeg egentlig med *transformering*? Jo, at vi gjør om til riktig
datatype. La oss se på et eksempel:

```
GET /smilefjes-rapport/2024
```

Her inneholder URL-en et årstall. Alt i en URL er strenger til å begynne med, så
det må fikses.

Like viktig som å validere at det er et årstall, er å gjøre strengen om til et
tall før det blir sendt videre inn i systemet.

## Konsekvenser

Vi får flere fordeler av å behandle parameterne våre på denne måten:

- Koden vår får være tydeligere på hva den gjør, og uttale seg i sikrere termer.

- De velkjente parameternavnene er med på å legge føringer, og hjelper oss å
  velge de samme navnene på de samme tingene.

- Vi håndterer ugyldige inn-data på samme måte på tvers av kodebasen.

Likevel er det ikke bare en dans på gull og grønne roser. Av og til vil det
komme feilaktige parametere fra vår egen kode. Bokstavelig talt *"the call came
from inside the house"*. Det løser vi, ikke ved å skrive mer defensiv kode,
men med flere tester.

Alternativet er at man strør om seg med assertions i brede lag av kodebasen. Det
ligner mer på statisk typing, hvor det er vanskeligere å gjøre ting feil, på
bekostning av at det er vanskeligere å gjøre ting riktig.
