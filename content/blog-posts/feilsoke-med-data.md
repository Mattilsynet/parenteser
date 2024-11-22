:page/title Feilsøking når alt er data
:blog-post/author {:person/id :person/christian}
:blog-post/published #time/ldt "2024-11-26T09:00:00"
:blog-post/tags [:clojure :datomic :nats]
:blog-post/description

Jeg pleier å si at vi jobber med data. Men hva betyr det? Bli med på en
feilsøking i landet der alt er data, så skal jeg vise deg hva jeg mener.

:blog-post/body

Jeg har tidligere skrevet om [hvordan vi bruker NATS til å importere
data](/nats-import-eksport/) fra et eldre system. Denne importen er under
kontinuerlig utvikling, og etter de siste endringene produserte den flere feil
enn vanlig. Opp med detektivlupen, vi skal på bugjakt!

Vi jobber med systemet vårt i et
[REPL](https://www.kodemaker.no/blogg/2022-10-repl/): server-prossessen kjører i
teksteditoren min, og jeg kan både be den om å gjøre ting og hente ut
informasjon fra den.

Første steg er å spørre kommando-consumeren om den siste exceptionen den hadde:

```clj
(def ex
  (->> integrant.repl.state/system ;; Det kjørende systemet
       :command/executor           ;; Kommando-consumeren
       deref                       ;; Få nåtilstanden
       :last-exception)            ;; Siste exception, takk
  )
```

Dette gir meg en exception, som i tillegg til å ha en melding og et stack trace
kan ha strukturerte data:

```clj
(.getMessage ex) ;;=> "Unable to effectuate effect"

(ex-data ex)
;;=>
{:basis-t 1029038
 :command/id #uuid "9ad2021f-2b16-4b17-9c7f-93e505616ec0"}
```

Dette betyr at en kommando har resultert i noen side-effekter som kræsja da
systemet ville utføre dem.

Vi fikk også noen nyttige opplysninger:

1. `:basis-t` er snapshotet av databasen som ble brukt til å behandle
   kommandoen.
2. `:command/id` er id-en på kommandoen som systemet behandlet da feilen
   oppstod.

Hvis vi blar videre i stack tracet finner vi denne meldingen fra Datomic:

```clj
  db.error/unique-conflict Unique conflict:
  :omfattet.vurdering/serveringssted+kriterium, value: [nil 17592186045441]
  already held by: 17592186046540 asserted for: 17592186046792
```

Jøssenavn. Her er det en konflikt på et unikt attributt. La oss se hva Datomic
har å si om entiteten vi er i konflikt med:

```clj
(d/entity app-db 17592186046540)
;;=>

{:db/id 17592186046540
 :omfattet.vurdering/serveringssted+kriterium [nil 17592186045441]
 ,,,}
```

Ja, der er vitterlig verdien som skapte konflikt. Men hva gjør den `nil`-en der?
Sånn skal det ikke være. Vi får be Datomic om å få se transaksjonen som
opprettet den `nil`-en:

```clj
;; Jeg kan kjøre queries mot transaksjoner på samme vis som annen data
(->>
  (d/q
   '[;; Finn transaksjonen
     :find ?t .

     ;; Med input-parameterne ?e og ?v
     :in $ ?e ?v

     ;; Hvor entiteten ?e får dette
     ;; attributtet satt til verdien ?v
     :where
     [?e :omfattet.vurdering/serveringssted+kriterium ?v ?t]]
   app-db
   17592186046540
   [nil 17592186045441])

  ;; Hent hele transaksjonsentiteten
  (d/entity app-db))
```

Ut får vi metadata om transaksjonen:

```clj
{:db/id 13194139535501
 :db/txInstant #inst "2024-11-22T14:37:44.094-00:00"
 :tx/mastermind :external-system/MATS
 :tx/command-kind :command/steng-serveringssted
 :tx/command-id #uuid "d034a9e7-7e79-4f30-8bec-4a4b970247a9"
 ,,,}
```

Ok, så en tidligere kommando for å stenge et serveringssted introduserte denne
feilen. Det var rart. Er det noe galt med kommandoen?

Kommandoene våre bor på en persistent strøm i NATS. Vi spør NATS, som også
gladelig gir oss rene data for penga:

```clj
(inspector-nats/get-command ctx #uuid "d034a9e7-7e79-4f30-8bec-4a4b970247a9")

;;=>
{:nats.message/subject "commands.steng-serveringssted.MATS.d034a9e7-7e79-4f30-8bec-4a4b970247a9"
 :nats.message/data
 {:command/mastermind :external-system/MATS
  :command/kind :command/steng-serveringssted
  :command/data
  {:tilsynsobjekt-id "Z2203291058010153110TGSVV_Tilsynsobjekt"
   :dato #inst "2023-04-30T22:00:00.000-00:00"}
  :command/id #uuid "d034a9e7-7e79-4f30-8bec-4a4b970247a9"}
 ,,,}
```

Nå kan jeg kan prøvekjøre kommandoen på nytt for å studere effektene den lager:

```clj
(commands/dispatch
 ;; Få databasen slik den var da kommandoen
 ;; opprinnelig ble behandlet
 {:db (d/as-of app-db 1029038)}

 command)
```

Dette kallet returnerer en beskrivelse av hva som ønskes utført:

```clj
{:outcome/effects
 [{:effect/kind :app/transact
   :effect/data
   [[:db/retractEntity [:omfattet.vurdering/serveringssted+kriterium 175952635652283]]
    [:db/retractEntity [:serveringssted/id "saw"]]
    {:db/id "datomic.tx"
     :hendelse/type :hendelse/stengte-serveringssted
     :hendelse/data {:dato #inst "2019-08-30T22:00:00.000-00:00"}}]}]}
```

Det er disse transaksjonsdataene det må være noe galt med, ettersom de fører til
at jeg får en `nil` i databasen.

Det var nok informasjon til å spore ned feilen. Den er ikke så interessant i seg
selv, men jeg kan avsløre at det ble iverksatt både facepalms og table flips. En
fantastisk dust feil, men med så gode verktøy til å feilsøke er det nesten
vanskelig å bli sur.
