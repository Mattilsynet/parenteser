:page/title Nedenfra og opp
:blog-post/author {:person/id :person/sigmund}
:blog-post/published #time/ldt "2025-03-13T12:15:00"
:blog-post/tags [:tdd]
:blog-post/description

Vi skal gjenskape et endepunkt som spyr ut enormt mye data. Koden for å
sammenlikne våre data med de fra den gamle tjenesten, kunne blitt ganske
innfløkt. La oss se på hvordan vi unngikk den fella.

:blog-post/body

De siste par ukene har Christian og jeg jobbet med å gjenskape en helt gigantisk
JSON saksbehandlingssystemet MATS produserer. Etter en masse graving i bruken av
disse dataene, oppdateringer i uttrekkene våre og å ha skrevet funksjoner for å
bygge opp tilsvarende data, var vi endelig klare for å sjekke at vi ikke hadde
gjort noe feil på veien. Selv om vi hadde jobbet test-drevet hele veien, regner
vi med at vi har gått glipp av noe underveis.

Derfor ville vi sammenlikne det vi svarte med, mot det MATS svarte med. Altså
trengte vi en strukturert diff over data med nøsting av maps og lister. Vi
visste at vi hadde noen forskjeller, for det er gjort mye datavask i importen
til vårt system. Vi har også sjekket klienten som henter dette, og vet at mange
ting ikke er i bruk, så dette har vi valgt å ikke ta med. Derfor vil vi ikke ha
ut alle mulige forskjeller, men bare de som er relevante.

Koden for å lage denne diffen, kunne blitt ganske innfløkt. Og da jeg først
begynte å kaste meg over den, jobbet jeg ovenfra og ned. Altså, jeg begynte med
å bygge en funksjon som kunne ta inn en generell datastruktur, og rekursivt
grave seg innover i den for å lage en liste med forskjeller. Da sa Christian noe
lurt: "jeg tenkte at vi kunne begynne med å håndtere en av de enkle datatypene."

## Flate data

Vi fant en av datatypene som ikke hadde noe nøsting. Flate data er hakket
enklere å sammenlikne enn maps med lister av maps med lister av maps. Så da
skrev vi først bare en liten funksjon for å sammenlikne akkurat denne datatypen,
noe à la:

```clj
(defn diff-virksomhetsinformasjon [mats-data matnyttig-data]
  (->> mats-data
      (map (fn [[k mats-val]]
             (let [matnyttig-val (get matnyttig-data k)]
               (cond
                 (= mats-val matnyttig-val)
                 nil
                 
                 (#{:foedselsnummer :orgform} k)
                 nil
                 
                 (and (= k :adresse)
                      (some not-empty (vals matnyttig-val)))
                 nil
                 
                 (and (= k :tlfnr)
                      (= matnyttig-val
                         (tlf/lokalt-nummer (tlf/vask-nummer mats-val))))
                 nil
                 
                 :else
                 {:sti [k]
                  :mats mats-val
                  :matnyttig matnyttig-val}))))
      (remove nil?)))
```

Allerede her kan vi se noen slags mønster. Vi har noen nøkler vi ikke bryr
oss om, og vi har noen som krever litt spesiell sammenlikning. Før vi skrev
dette om til en generell funksjon for alle flate data, prøvde vi oss på en annen
datatype som heller ikke hadde noen nøsting. Til slutt endte vi opp med denne
funksjonen:

```clj
(defn diff [mats-data matnyttig-data & [{:keys [ignore-ks diff-ks sti]}]]
  (->> (keys mats-data)
       (remove (or ignore-ks #{}))
       (keep (fn [key]
               (let [mats-val (get mats-data key)
                     matnyttig-val (get matnyttig-data key)
                     like? (get diff-ks key)]
                 (cond
                   (= mats-val matnyttig-val)
                   nil

                   (and like? (like? mats-val matnyttig-val))
                   nil

                   :else
                   {:sti (conj (or sti []) key)
                    :mats (get mats-data key)
                    :matnyttig (get matnyttig-data key)}))))))
```

Om du synes det er vanskelig å lese, gjør koden følgende: hent alle nøklene fra
original-dataene fra MATS, fjern de vi ikke er interessert i og ta ut alle
forskjellene. Disse forskjellene finner vi ved å se om verdiene er like, enten
identiske eller basert på en sammenlikningsfunksjon for nøkkelen. Diffen vi får
tilbake ser omtrent ut som dette:

```clj
[{:sti [:navn]
  :mats "Krusty Burger"
  :matnyttig "Kentucky Fried Panda"}
 {:sti [:tlfnr]
  :mats "001   555-KRUSTY"
  :matnyttig "+1555PANDA"}]
```

## Nøstede data

Hele veien skrev vi tester som sjekket at vi fikk ut de forskjellene vi bryr oss
om med stadig flere datatyper. Så kom vi til slutt til den kjipe typen som
inneholdt lister med andre sammensatte typer, som igjen inneholdt lister med
sammensatte typer.

Det var enkelt å sjekke alt som bare var enkle verdier på toppnivå, bare legg
listene til de ignorerte verdiene. Så måtte vi sjekke listene. Igjen begynte vi
med en av listene. Rekkefølgen i lista skulle vi ikke bry oss om. Så vi begynte
med noe sånt som:

```clj
(let [mats-vals (into {}
                  (map (fn [v] [(:id v) v])
                       (:vedtak mats-data)))
      matnyttig-vals (into {}
                       (map (fn [v] [(:id v) v])
                            (:vedtak matnyttig-data)))
      mats-key-set (set (keys mats-vals))
      matnyttig-key-set (set (keys matnyttig-vals)))]
  (if (not= mats-key-set matnyttig-key-set)
    [{:sti [:vedtak]
      :mats mats-key-set
      :matnyttig matnyttig-key-set})
    (mapcat (fn [[id mats-val]]
              (diff-vedtak mats-val
                           (get matnyttig-vals id)
                           {:sti [:vedtak id]})))))
```

`diff-vedtak` er bare en liten hjelpefunksjon som legger på ignorerte
nøkler og sammenlikningsfunksjoner i kallet på `diff`. Så gikk vi løs på neste
type liste og skrev dette om til en generell funksjon for å løpe gjennom og
sammenlikne innholdet i lister. Den endelige funksjonen ble ikke så altfor
forskjellig fra kodesnutten ovenfor:

```clj
(defn diff-liste [mats-data matnyttig-data k & [{:keys [id-f sti] :as opt}]]
  (let [id-f (or id-f :id)
        mats-vals (map-by id-f (get mats-data k))
        matnyttig-vals (map-by id-f (get matnyttig-data k))
        id-er (into (set (keys mats-vals))
                    (keys matnyttig-vals))
        sti (conj (or sti []) k)
        diff-f (or (:diff opt) diff)]
    (mapcat
     (fn [id]
       (let [mats-val (get mats-vals id)
             matnyttig-val (get matnyttig-vals id)
             sti (conj sti id)]
         (if (or (nil? mats-val) (nil? matnyttig-val))
           [{:sti sti
             :mats (if (nil? mats-val) :mangler :til-stede)
             :matnyttig (if (nil? matnyttig-val) :mangler :til-stede)}]
           (diff-f mats-val (get matnyttig-vals id)
                   (assoc opt :sti sti)))))
     id-er)))
```

Gjett hva hele datasettet består av! En map med lister i hver eneste verdi.
Datastrukturen hadde fire nivåer med nøsting av lister og vi endte med totalt
tolv forskjellige lister å sammenlikne i dette ene endepunktet. Så disse
semi-generelle funksjonene har fått blitt brukt ganske mange ganger. Skulle vi
gjort det samme med en helt generell diff-funksjon, måtte vi nok ha massert
dataene på mye mer kronglete måter og hatt typesjekker på verdier for å håndtere
nøstinga.

Etter å ha blitt med i Team Servering, har jeg gjenlært mange gamle lekser. Vi bruker
TDD og par-programmering aktivt, noe jeg var med å lære nyansatte i min første
jobb, selv om vi nesten aldri brukte det i praksis der. Litt klassisk "gjør som
jeg sier, ikke som jeg gjør", og så gjør ingen det vi sier at vi skal gjøre.

Det andre er at det ofte lønner seg å jobbe nedenfra og opp for å bryte opp
problemer i håndterbare biter. Da deler det seg litt opp av seg selv og
generelle mønstre dukker opp i prosessen istedenfor at man skal analysere seg
fram til det fra et fugleperspektiv.
