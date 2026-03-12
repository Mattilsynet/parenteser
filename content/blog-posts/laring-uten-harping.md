:page/title Læring uten herping
:blog-post/author {:person/id :person/teodor}
:blog-post/published #time/ldt "2026-03-12T14:23:38.991656"
:blog-post/tags []
:blog-post/description

Et prinsipp utvikling av programmer, systemer og produkter

:blog-post/body

*et prinsipp utvikling av programmer, systemer og produkter*

Hvordan gjør vi en ting bedre?
Det er et overraskende vanskelig spørsmål med et enkelt svar: vi forbedrer uten å forverre.

Målet er altså:

- kontinuerlig læring og verdiskaping
- uten herping.

Kontinuerlig læring og verdiskaping forutsetter at vi kan forme
om virkeligheten. At vi ikke skal herpe det til for folk forutsetter
at det vi lager er pålitelig, og at fundamentet det vi lager står på
også er pålitelig.

Å forme om i isolasjon er lett: kast terning og rull ut!
Tilsvarende er pålitelighet i isolasjon lett: bare la det være.
Utfordringen er hvordan vi får til *begge to* samtidig.

## formbarhet og pålitelighet

Målet er formbar og pålitelig programvare. Vi kan forme om
programvaren etter behov, uten at omformingen går ut over
pålitelighet.

Hvordan gjør vi det i praksis med koden vår?
Vi har to alternativer:

1. Vi former koden ved å endre eksisterende kode, og passer på at vi ikke brekker ting.

2. Vi former koden ved å legge til ny kode ved siden av
   eksisterende kode. Påliteligheten er sikret ved at eksisterende,
   levert oppførsel er uendret.

Jeg foretrekker nummer to, fordi den er lettest.
Pålitelighet er løst som default i systemet vårt.
Når vi legger til ny funksjonalitet, slipper vi utilsiktet tukling med eksisterende funksjonalitet.
Da er det mindre som kan gå galt, og lettere for meg å gjøre jobben min.

## additativ programmering

Denne programmeringsdisiplinen kalles *additativ programmering* av Chris Hanson og Gerald Sussman i *[Software Design for Flexibility]*.
Det gjør vi ved at vi løser for forgrening rett i systemdesignet, og unngår flere og flere nøstede if-setninger.
Hanson og Sussman viser noen eksempler i boka, deriblant generisk dispatch og layering.
Generisk dispatch og layering er sinnsykt spennende teknikker, men dekkes ikke i denne teksten.

## dataorientert dispatch

Christian og Magnar har etablert en annen dispatch-teknikk i Mattilsynet-kodebasen vår.
Benny Andersson omtalte denne som "ekstrem dataorientering".
Alternativt kunne vi snakket om "dataorientert dispatch".
Sidesystemet vårt er et eksempel.
Sidedefinisjonene ser slik ut:

```clojure
{:page/id :pages/statistikk
 :page/route ["statistikk"]
 :page/render #'render}
```

Systemet blir additativt fordi nye sider legges til additativt.
Ny render-funksjon.
Nytt hash-map.
Ingen nye if-er.

Sidedefinisjonene gir oss også ett sted å løse siderelaterte problemer.
Vi bruker sidedefinisjonene til å *lage* routeren.
Men vi bruker også sidedefinisjonene til å lage sidekartet ("sitemap").

## etterord: Sussman

Software Design for Flexibility er kanskje den beste boka om design av
programvare ingen av lest. Java-standarden ble skrevet av Guy Steele, Sussman
var veilederen til Steele. At hele boka er skrevet med eksempler i Scheme gir en inngangsterskel for lesing.
Men Scheme er et minimalistisk språk.
Og det var Sussman som lagde Scheme.
Så ideene henger sammen, selv om det krever litt innsats å "dykke ned".

## etterord: formbarhet og pålitelighet når LLM-er tukler med koden

Industrien vår har sniffet LLM, og volumet på festen er stigende.
Folka som dansser på stranda har ikke ennå sett søpla som ansamler seg rett bak klippene.
Hvorvidt hver og én av oss klarer å nyttiggjøre oss LLM-ene kommer til å bli bestemt av formbarhet og mykhet i kodebasen vår.
Kalsifiserer LLM-en kodebasen med vaghet og leoparder som gjør den uleselig?
  Gjør den kodebasen upålitelig, og akkumulerer bugs ingen klarer å finne?
  Eller klarer vi å holde på mykheten og formbarheten?
Gråskjegg som Kent Beck og Steve Yeggie sier de får til nettopp det, og oppskriften de gir er ikke å la LLM-en gå amok.
Det er å bli enda bedre på formbarhet og pålitelighet i kode.
Tydeligere API-design og tydeligere teststrategi.
Pålitelighet gir formbarhet.

[Software Design for Flexibility]: https://mitpress.mit.edu/9780262045490/software-design-for-flexibility/
