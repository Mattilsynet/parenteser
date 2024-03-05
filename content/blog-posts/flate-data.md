:page/title Flate, møre data
:blog-post/author {:person/id :person/christian}
:blog-post/published #time/ldt "2024-03-05T09:00:00"
:blog-post/tags [:clojure :datamodellering]
:blog-post/description

Å banke en kyllingfilet flat med en kjøttbanker er nøkkelen til en saftig og
smakfull suksess på kjøkkenet. Sånn er det med data også: Flate data er bedre
enn nøsta data -- men hvorfor?

:open-graph/description

Flate data er bedre enn nøsta data, men hvorfor er det sånn?

:blog-post/body

En liste med tall er en flat datastruktur. En liste med maps, der hvert map
representerer en person, og hvert person-map har en liste med maps som
representerer en bloggpost, og hver bloggpost har en... Vel, du skjønner -- er
en nøsta datastruktur. Det er ikke nødvendigvis enten/eller, men heller et
spekter. Du kan flate en datastruktur helt ut, men det er ingen grenser for hvor
dypt du kan nøste den (du kan sågar lage sirkulære datastrukturer).

Min påstand er at flatere er bedre.

## En av mange mulige projeksjoner

Et eksempel letter samtalen. Nedenfor ser du et Clojure-map som representerer en
[pull request](/pull-requests/). Den har kommentarer, og hver kommentar har en
forfatter.

```clj
{:id "12"
 :title "Add the thing-a-majig"
 :comments
 [{:created-at #inst "2024-02-12T07:56:04Z"
   :text "LGTM!"
   :author {:username "alice"
            :name "Alice"}}
  {:created-at #inst "2024-02-12T07:57:13Z"
   :text "Are you sure? I don't like it"
   :author {:username "bob"
            :name "Bob"}}
  {:created-at #inst "2024-02-12T08:02:56Z"
   :text "Come on, Bob. Really?"
   :author {:username "alice"
            :name "Alice"}}]}
```

Den første observasjonen vi kan gjøre er at en nøsta datastruktur er én av mange
mulige projeksjoner av den underliggende informasjonen. Hvis vi ser på
datastrukturen som et tre så kan jeg ta tak i en hvilken som helst av nodene og
løfte den opp som rotnode og få en ny projeksjon.

Eksempelvis kan jeg orientere datastrukturen rundt personen i stedet:

```clj
{:username "alice"
 :name "Alice"
 :comments
 [{:created-at #inst "2024-02-12T07:56:04Z"
   :text "LGTM!"
   :pull-request {:id "12"
                  :title "Add the thing-a-majig"
                  ,,,}}
  {:created-at #inst "2024-02-12T08:02:56Z"
   :text "Come on, Bob. Really?"
   :pull-request {:id "12"
                  :title "Add the thing-a-majig"
                  ,,,}}]}
```

Hvilken av disse som passer best kommer an på formålet. Så kan man spørre seg om
hvorvidt én konkret kontekstuell organisering er den beste måten å representere
data på?

## Dupliserte data

I en nøstet datastruktur får vi fort duplisert data. I vårt første eksempel var
det forfatterne som dukket opp flere ganger, og når vi snudde fokuset mot
forfatterne så måtte vi gjenta informasjon om pull requesten.

Ok, så vi dupliserer noen byte med data, er det så farlig da? Igjen: det kommer
an på konteksten. At det blir noen bytes ekstra når klienten henter litt data
fra serveren tror jeg ingen bryr seg nevneverdig om.

Men hva med klienten? Når det samme datapunktet finnes mange steder blir det
veldig mye vanskeligere for klienten å vise konsekvente opplysninger. GitHub har
slitt mye med problemer av denne typen, eksempelvis: Når du lukker en pull
request, oppdaterer mye av UI-et seg -- men tallet på åpne pull requests i
menyen forblir det samme.

## Flatere data

La oss gå tilbake til vår pull request-orienterte struktur og flytte brukerne ut
av tre-strukturen:

```clj
[{:id "12"
  :title "Add the thing-a-majig"
  :comments
  [{:created-at #inst "2024-02-12T07:56:04Z"
    :text "LGTM!"
    :author "alice"}
   {:created-at #inst "2024-02-12T07:57:13Z"
    :text "Are you sure? I don't like it"
    :author "bob"}
   {:created-at #inst "2024-02-12T08:02:56Z"
    :text "Come on, Bob. Really?"
    :author "alice"}]}

 {:username "alice"
  :name "Alice"}
 {:username "bob"
  :name "Bob"}]
```

Ved å flate det hele ut unngår vi dupliseringen, og gir oss selv mindre rom for
feil. Legg merke til at dette bare er litt flatere enn orginalen -- det er
gevinster å hente uten å flate alt helt ned.

Det er også verdt å merke seg at denne utflatingen er en form for
normalisering - noe databaser er veldig gode på. Og visst er databaser nyttige
overalt hvor du har data, også [på
frontenden](https://www.kodemaker.no/blogg/2019-06-datascript/).

## Addresserbarhet

Utflatingen av pull requesten introduserte et problem: `:author "alice"` er en
veldig løs kobling til bruker-mappet lenger ned. La oss gjøre den noe mer
presis.

Jeg har nylig skrevet om [nøkler og deres bruk](/nokler/), hvor vi så hvordan
navnerom på nøkler gjør at de kan ha global semantikk. Dette kan også gi oss
adresserbarhet.

```clj
[{:pull-request/id "12"
  :pull-request/title "Add the thing-a-majig"
  :pull-request/comments
  [{:comment/created-at #inst "2024-02-12T07:56:04Z"
    :comment/text "LGTM!"
    :comment/author [:user/username "alice"]}
   {:comment/created-at #inst "2024-02-12T07:57:13Z"
    :comment/text "Are you sure? I don't like it"
    :comment/author [:user/username "bob"]}
   {:comment/created-at #inst "2024-02-12T08:02:56Z"
    :comment/text "Come on, Bob. Really?"
    :comment/author [:user/username "alice"]}]}

 {:user/username "alice"
  :user/name "Alice"}
 {:user/username "bob"
  :user/name "Bob"}]
```

Den løse strengen `"alice"` er byttet ut med et tuple: `[:user/username
"alice"]`. Dette krever fortsatt noe forhåndskunnskap for å tolke, men vi har nå
én generisk mekanisme som kan beskrive alle referanser i datasettet -- i stedet
for å måtte kode spesialregler for alle de konkrete koblingene.

Jeg har ikke dratt denne representasjonen ut av løse lufta. Dette er nemlig
hvordan [Datomic](/smakebiter-av-datomic/) representerer koblinger. Datomic har
i tillegg et skjema å støtte seg på, som sier noe om hva som kan være en
kobling, og hvilke ting du kan lage koblinger til, slik at den kan forstås uten
forhåndskunnskaper om datasettet.

## Flere Flate Fordeler

Se for deg at en kommentar blir redigert. Med den nøsta datastrukturen måtte vi
da hentet ut hele pull requesten på nytt. Og hvis vi skulle identifisert hva som
har endret seg så ville vi sannsynligvis pekt på både pull requesten og
kommentaren, selvom pull requesten bare er et uskyldig mellomledd.

Hva om vi flater ut strukturen ytterligere?

```clj
[{:pull-request/id "12"
  :pull-request/title "Add the thing-a-majig"}

 {:comment/id 1234
  :comment/created-at #inst "2024-02-12T07:56:04Z"
  :comment/text "LGTM!"
  :comment/author [:user/username "alice"]
  :comment/pull-request [:pull-request/id "12"]}
 {:comment/id 1235
  :comment/created-at #inst "2024-02-12T07:57:13Z"
  :comment/text "Are you sure? I don't like it"
  :comment/author [:user/username "bob"]
  :comment/pull-request [:pull-request/id "12"]}
 {:comment/id 1236
  :comment/created-at #inst "2024-02-12T08:02:56Z"
  :comment/text "Come on, Bob. Really?"
  :comment/author [:user/username "alice"]
  :comment/pull-request [:pull-request/id "12"]}

 {:user/username "alice"
  :user/name "Alice"}
 {:user/username "bob"
  :user/name "Bob"}]
```

I denne representasjonen er det trivielt å pinpointe endringer, samtidig som vi
lett kan bygge den opprinnelige nøsta strukturen når vi trenger det. Den
nysgjerrige leser kan sjekke ut to alternative måter å gjenskape trærne med
fokus på henholdvis pull requests og brukere i [denne
gisten](https://gist.github.com/cjohansen/22b2164e72221205ee97cc821abbcff5).

For et annet konkret eksempel på hva man kan få til med dette kan du sjekke ut
[Datoms differ](https://github.com/magnars/datoms-differ), som med hjelp av et
skjema kan flate ut vilkårlige (til og med sirkulære) datastrukturer og diffe
dem.

Når vi flater ut data plukker vi informasjonen fra hverandre. Dette åpner døren
for nye bruksområder og reduserer duplisering uten at vi mister noe -- fordi den
nøstede strukturen vi startet med lett kan gjenskapes fra de flate dataene.
