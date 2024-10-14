:page/title Akkurat passe integrasjonstester
:blog-post/author {:person/id :person/magnar}
:blog-post/published #time/ldt "2024-10-15T09:00:00"
:blog-post/tags [:testing :integrasjonstester]
:blog-post/description

På hvilket nivå skal man legge sine tester? Hvor mange er nok? Når blir det for
mange? Jeg har ikke svar med to streker under, men jeg skrev et par fine
integrasjonstester nylig og har noen tanker.

:blog-post/body

Det er ikke alltid så lett å vite hvordan man skal tilnærme seg testing.
Christian hadde en aldeles nydelig take på det i [sitt
foredrag](/hvordan-levere-kontinuerlig/) på årets JavaZone:

> Hvor mange tester skal vi ha? Hvilke tester skal vi skrive? Hva slags tester
> skal vi skrive? Det går det ikke an å gi noe kvantitativt svar på, men jeg kan
> svare med en ... følelse!
>
> Hvis du kjører alle testene dine, og alle sammen passerer og er grønne, så
> kommer jeg til å automatisk oppdatere prodsystemet ditt UTEN at et menneske
> går og sjekker at det funker.
>
> Hvis du er komfortabel med det, så har du nok tester.

Det er nettopp denne følelsen av komfort jeg lener meg på for å dytte rett i
prod. Og det er denne følelsen av komfort som ble brutt her forleden dag når
forsiden brakk i prod. Selv om Christian og jeg utgjør 66,67% av brukerne av prod
akkurat nå, så ble det tydelig at denne senga trengte flere puter.

## En komfortabel pute

Jeg skrev en test for å reprodusere feilen:

```clj
(deftest kan-rendres-med-tom-database
  (is (= (:page/title
          (with-ctx [ctx]
            (test-refinery/render-page ctx "/")))
         "Ikke så matnyttig om smil")))
```

Det er noe som er rart her. Rart og fint, vil jeg si, men du skal få gjøre opp
din egen mening. La oss se litt nærmere sammen på hva som skjer og hvorfor.

```clj
(with-ctx [ctx]
  ...)
```

Dette er en testhjelper som fyrer opp en testversjon av systemets
avhengigheter, slikt som ferdig migrerte databaser i minnet og annen kos.

```clj
(test-refinery/render-page ctx "/")
```

Her ber jeg om å få rendret siden som er å finne på URL-en `"/"`. Igjen bruker
jeg en hendig testhjelper.

Til slutt sjekker jeg at tittelen på siden samsvarer med mine forventinger:

```clj
(= (:page/title ...)
   "Ikke så matnyttig om smil")
```

"Ikke så matnyttig om smil" er tittelen når vi ikke finner noe data.

Og det var nettopp en tom database som brakk forsiden vår. Import-jobben hadde
ikke kjørt, databasen var tom, og koden som skulle rendre forsiden kastet en
exception.

#### Hva testes egentlig her?

Vi tester utvilsomt at:

- Forsidens sidetittel blir riktig.

Det er mest åpenbart. Samtidig er det minst interessant. Det er det jeg
mener er litt rart. De viktigste tingene ligger skjult under panseret. Her er
de mer spennende tingene vi tester:

- Forsiden er kablet opp i koden.
- Det routes riktig fra `"/"` til forsiden.
- Dataene til forsiden settes sammen uten exceptions.
- Forsiden rendres uten exceptions.

Dette er det jeg ønsket meg ut av testen. Den sørger for at forsiden kan bygges
og serveres på et overordnet plan.

#### Hva testes ikke?

Kanskje like viktig er hva vi ikke tester. Nemlig:

- Hvilken informasjon er å finne på forsiden?
- Hvordan fungerer logikken på forsiden?

Dette er jobben til enhetstester. Det er unødvendig å blande maskineriet for
routing og datafangst inn i de minste logikktester. De blir unødvendig tunge å
skrive og å kjøre.

Vi tester heller ikke:

- At vi får startet en webserver som svarer på HTTP-requests på en gitt port.
- At vi får statuskode 200 med riktige Content-Type headers.
- At vi har konfigurert oppkobling mot databasene våre riktig.
- At kablingen av hele systemet fungerer som det skal.

Dette er jobben til en mer generell integrasjonstest, aller ytterst. Vi har ikke
lyst til å se 90 brukne tester når én ting i maskineriet slutter å funke. Det
blir litt som å komme på hotellrommet og oppdage at halve senga er dekket av et
berg fullstendig unødvendige puter. Ellers takk, så "komfortabelt" trenger jeg
ikke ha det.

## Det som var fint

Og da har vi kommet til det jeg syns er fint. Denne testen havnet på et bra
nivå. Et komfortabelt mellomsjikte, hvor den kun tester at siden er satt opp
riktig, uten å gå i for mye detaljer. Den overlater detaljer om
forsideimplementasjonen til enhetstester, og detaljer om systemet til en mer
generell integrasjonstest.

Det betyr at jeg med god samvittighet kan skrive slike tester til alle sidene vi
har, uten å bekymre meg for at de skal brekke for et godt ord. Da blir det
lettere å dytte kode ut i prod med lett hjerte. Og det er bra, ikke bare for
[koden min](https://dora.dev/capabilities/continuous-delivery/), men også [for meg](https://2024.javazone.no/program/67548ebd-21cb-495f-a21a-5432e95757d4).
