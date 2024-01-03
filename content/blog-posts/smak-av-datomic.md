:page/title Smakebiter av Datomic: En eksplosjon av data
:blog-post/author {:person/id :person/magnar}
:blog-post/published #time/ldt "2024-01-03T15:45:00"
:blog-post/tags [:datomic :clojure]
:blog-post/description

Datomic er en aldeles nydelig database å jobbe med. Vi starter året med en ny
serie med smakebiter av denne funksjonelle, funksjonelle databasen. Først ut er
datamodellen som ligger i bunn -- og eksplosjoner!

:open-graph/description

Datomic er en nydelig database å jobbe med. Vi starter året med en ny
serie med smakebiter av denne funksjonelle, funksjonelle databasen. Først ut er
datamodellen som ligger i bunn -- og eksplosjoner!

:blog-post/body

Datomic er en aldeles nydelig database å jobbe med. Vi starter året med en ny
serie med smakebiter av denne funksjonelle, funksjonelle databasen. Først ut er
datamodellen som ligger i bunn -- og eksplosjoner!

#### Sa du eksplosjoner?

Ja visst!

Datomic lagrer alle dataene sine flatt, men de dataene vi jobber med er jo
sjelden det. La oss ta et eksempel:

```clj
{:blog-post/id 14
 :blog-post/title "Smakebiter av Datomic: En eksplosjon av data"
 :blog-post/tags [:datomic :clojure]
 :blog-post/author {:person/id "magnars"
                    :person/given-name "Magnar"
                    :person/family-name "Sveen"}}
```

Her er det jo et map, med en liste og et nytt map inni. Vi kunne kanskje lagret
dette akkurat slik i en dokumentdatabase? -- men datomic er ikke en sånn'en. Dessuten har
jeg en mistanke om at den personen der er forfatter av andre bloggposter også.

Vi må bryte det opp på noe vis.

#### Ergo, eksplosjoner?

Ja, men først litt om datamodellering.

Tradisjonelle relasjonelle databaser [sliter med å modellere
trestrukturer](https://en.wikipedia.org/wiki/Object–relational_impedance_mismatch).
Datomic svingte rundt hele problemet ved i stedet å basere seg på idéer fra
universell datamodellering, som
[RDF](https://en.wikipedia.org/wiki/Resource_Description_Framework) sine
[semantiske tripler](https://en.wikipedia.org/wiki/Semantic_triple).

Istedenfor rader og kolonner i firkantede tabeller, er dataene organisert i
entiteter og attributter.

Det ser nesten slik ut:

```clj
[entitet, attributt, verdi]
```

Eksempelvis:

```clj
[1234, :blog-post/title, "Smakebiter av Datomic: En eksplosjon av data"]
```

Datomic sin nyvinning i dette spacet er å legge til *tid* i dette triplet. *Når*
er denne informasjonen fra?

```clj
[entitet, attributt, verdi, tid]
```

Triplet har blitt et **datom**.

#### Jeg mener bestemt at jeg ble lovet eksplosjoner

Det kommer nå!

Vi hadde jo denne, ikke sant?

```clj
{:blog-post/id 14
 :blog-post/title "Smakebiter av Datomic: En eksplosjon av data"
 :blog-post/tags [:datomic :clojure]
 :blog-post/author {:person/id "magnars"
                    :person/given-name "Magnar"
                    :person/family-name "Sveen"}}
```

Datomic eksploderer denne ut i sine minste bestanddeler, slik at alt kan
lagres flatt. La oss starte innerst:

```clj
[5678, :person/id, "magnars"]
[5678, :person/given-name, "Magnar"]
[5678, :person/family-name, "Sveen"]
```

Her har vi altså en entitet med en intern ID `5678`, og alt vi vet om den.

```clj
[1234, :blog-post/id, 14]
[1234, :blog-post/title, "Smakebiter av Datomic: En eksplosjon av data"]
[1234, :blog-post/author, 5678]
```

Se her kom et triks!

Blogposten har en referanse til en annen entitet. I Datomic har man et skjema
som beskriver hvilke attributter som peker på andre entiteter, såkalte *refs*.

Til slutt, lista med tags:

```clj
[1234, :blog-post/tags, :datomic]
[1234, :blog-post/tags, :clojure]
```

Her skjer det noe rart.

Igjen må vi bruke Datomic sitt skjema til å si at dette attributtet har høy
kardinalitet -- altså at det kan ha flere verdier per attributt.

Og med det har vår nøsta datastruktur blitt eksplodert til tripler:

```clj
[1234, :blog-post/id, 14]
[1234, :blog-post/title, "Smakebiter av Datomic: En eksplosjon av data"]
[1234, :blog-post/author, 5678]
[1234, :blog-post/tags, :datomic]
[1234, :blog-post/tags, :clojure]
[5678, :person/id, "magnars"]
[5678, :person/given-name, "Magnar"]
[5678, :person/family-name, "Sveen"]
```

Vakkert.

#### Og poenget er?

Hovedpoenget er at denne tilnærmingen modellerer både grafer og relasjonelle
data -- aldeles fri for [impedance
mismatch](https://en.wikipedia.org/wiki/Object–relational_impedance_mismatch).

I tillegg: Når man har sprengt trestrukturen fra hverandre, så er dataene mye
lettere å jobbe med under panseret. De har alle samme form, som åpner for mange
muligheter. Du kan for eksempel [strømme data til nettleseren uten å lage det på nytt hver
gang](https://vimeo.com/289851906), eller [finne forskjeller mellom vilkårlige
datasett på en effektiv måte](https://github.com/magnars/datoms-differ).

I neste bloggpost i serien skal vi se hvordan Datomic bruker dette til effektiv
indeksering av data. Det er bare å glede seg!
