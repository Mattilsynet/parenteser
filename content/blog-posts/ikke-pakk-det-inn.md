:page/title Ikke pakk inn godsakene
:blog-post/author {:person/id :person/magnar}
:blog-post/published #time/ldt "2024-12-10T09:00:00"
:blog-post/tags [:clojure :design]
:blog-post/description

Jula nærmer seg, og det er klart: Gaver må pakkes. Men det er ingen
presang til de andre på teamet om du pakker inn funksjonaliteten du skriver
i for mye glorete papir.

:blog-post/body

Jula nærmer seg, og det er klart: Gaver må pakkes. Men det er ingen
presang til de andre på teamet om du pakker inn funksjonaliteten du skriver
i for mye glorete papir.

Her er et eksempel. Det er en liten munnfull, så ikke heng deg opp i detaljene.
Kort fortalt så oppdaterer funksjonen poststedet på en adresse, slik at både
`"mo i rana"` og `"MO I RANA"` blir til det normaliserte `"Mo i Rana"`:

```clj
(defn normaliser-poststed [adresse]
  (assoc adresse :poststed
         (->> (for [ord (map str/lower-case (str/split (:poststed adresse) #" "))]
               (if (#{"og" "i" "på"} ord)
                 ord
                 (->> (str/split ord #"-")
                      (map str/capitalize)
                      (str/join "-"))))
             (str/join " "))))
```

Kan du se hva som er feil? Her er det noen godsaker, men de er pakket inn.
Funksjonen gjør en antagelse. Den antar at den alltid skal jobbe på en adresse -- alltid et poststed.

Hva om jeg har lyst til å normalisere et stedsnavn som ikke er poststedet til en
adresse? La oss rive av julepapiret:

```clj
(defn normaliser-stedsnavn [s]
  (->> (for [ord (map str/lower-case (str/split s #" "))]
         (if (#{"og" "i" "på"} ord)
           ord
           (->> (str/split ord #"-")
                (map str/capitalize)
                (str/join "-"))))
       (str/join " ")))

(defn normaliser-poststed [adresse]
  (update adresse :poststed normaliser-stedsnavn))
```

Se så, mye bedre! Nå er godsakene dratt ut til en gjenbrukbar funksjon, og
`normaliser-poststed` bruker den til å oppdatere poststedet på en adresse.

### Julepapirets skjebne

Ja, for hva gjør man egentlig med julepapiret? På fredag hadde jeg besøk av
renovasjonsavdelingen i Fredrikstad Kommune, som bandt en stor rosa pose på
søppeldunken min. "Julepapir" stod det på den.

For trenger vi egentlig `normaliser-poststed` nå?

Jeg kan like gjerne skrive dette selv:

```clj
(update adresse :poststed normaliser-stedsnavn)
```

Jeg trenger ikke at den er pakket inn for meg.

## Et par andre gjengangere

Hvis du tar deg selv i å være behjelpelig og pakke inn godsakene med en standard
Clojure funksjon, så er det antagelig mer hjelpsomt å la være.

Her er noen flere eksempler:

```clj
(wrap-gifts gifts)

;; =>

(map wrap-gift gifts)
```

Jeg trenger ikke hjelp til å kalle samme funksjon på mange elementer i en liste.
Lag funksjonen for å pakke inn én pakke, så bruker jeg `map` når jeg har mange å
pakke inn. 🎁 😄

```clj
(remove-naughty-kids kids)

;; =>

(remove naughty? kids)
```

Se, det holder med en `naughty?`-funksjon! Ikke bare leser den andre varianten
bedre, men nå kan jeg bruke den med både `filter` og `remove` ved behov.

## Til slutt

Det er masse fine byggeklosser i [clojure.core](/clojure-core/). Lag nyttige små
funksjoner, uten å pakke dem inn, så kommer de til sin rett. Blir så bra, atte.

Og god jul da, du!
