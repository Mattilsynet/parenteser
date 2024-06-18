:page/title Hvilke data kommer her?
:blog-post/author {:person/id :person/magnar}
:blog-post/published #time/ldt "2024-06-18T09:00:00"
:blog-post/tags [:clojure]
:blog-post/description

Det hender at jeg ser på en ukjent funksjon og lurer på "Hva slags data blir
sendt inn her?" I et språk som Clojure uten statiske typer, hvordan håndterer
jeg den situasjonen?

:blog-post/body

Det hender at jeg ser på en funksjon det er lenge siden jeg skrev - eller som
noen andre har lagt til - og så lurer jeg på "Hva slags data blir sendt inn
her?"

I et statisk typet språk kan man se på typesignaturen hva slags data det er lov
å sende inn, men i et dynamisk typet språk kan det komme *hva som helst*.
Helsprøtt opplegg!

La oss ta en titt på hvordan jeg klarer meg bra i en sånn verden.

## 1. Se på testene

Noe av det første jeg gjør hvis jeg blir usikker er å hoppe over til testene.
Der finner jeg som oftest et eksempel eller to på hvordan funksjonen brukes.
I så tilfelle slutter jeg å lure allerede i dette punktet.

Det er åpenbart at tester er viktigere når man jobber i et dynamisk språk enn et
statisk. Ikke så mye viktigere at du like greit kan droppe det i Kotlin, liksom,
men de blir mer verdifulle. Man lener seg mer på dem.

Akkurat det får andre konsekvenser også: Når jeg til stadighet får glede og
nytte av testene, så øker fokuset mitt på å skrive gode tester. Det gir igjen
bedre kode. Testene tvinger meg til å skrive mer modulær kode. Det aller
enkleste å teste er rene funksjoner - og slike vil jeg ha så mange som mulig av
i kodebasen. Det er en oppadgående spiral av kos.

Uansett, det var et sidespor, videre til neste strategi:

## 2. Bruk [REPL-et](https://www.kodemaker.no/blogg/2022-10-repl/)

Utvikling med Clojure foregår *inni* koden. Det er sånn det føles. Jeg har hele
tiden prosessen kjørende sammen med meg. Jeg evaluerer koden, og får svar rett
tilbake.

La oss si at jeg har denne funksjonen her:

```clj
(defn finn-senter [state serveringssted]
  (when-let [postnummer (:postnummer (:adresse serveringssted))]
    (let [sentere (senter/finn-sentere-i-kommune state postnummer)]
      (or (adresser/finn-senter-fra-adresse sentere (:adresse serveringssted))
          (senter/finn-senter-fra-navn sentere [(:navn serveringssted)])))))
```

Jeg skulle gjerne hatt tak i eksempel på `serveringssted` - men jeg finner ingen
tester som kaller funksjonen min direkte.

Da kan jeg midlertidig endre definisjonen slik:

```clj
(defn finn-senter [state serveringssted]
  (def mitt-serveringssted serveringssted) ;; <==
  (when-let [postnummer (:postnummer (:adresse serveringssted))]
    (let [sentere (senter/finn-sentere-i-kommune state postnummer)]
      (or (adresser/finn-senter-fra-adresse sentere (:adresse serveringssted))
          (senter/finn-senter-fra-navn sentere [(:navn serveringssted)])))))
```

Jeg evaluerer den nye funksjonen inn i REPLet, og kjører koden. Det kan jeg
gjøre via nettleseren min, for eksempel, ved å trykke litt rundt i UI-et.
Etterpå kan jeg gå tilbake og evaluere `mitt-serveringssted` og se hva som kom.

```clj
mitt-serveringssted

;; =>

{:adresse {:linje1 "Lufthavnveien 3"
           :postnummer "2060"}
 :navn "Yumsies Oslo Lufthavn Gardermoen"}
```

Det er litt frekt.

I dette tilfellet så var det faktisk også tester å lene seg på:

```clj
(deftest finn-senter-test
  (testing "Finner senter fra navn"
    (is (= (with-ctx [ctx]
             (->> {:adresse
                   {:linje1 "Lufthavnveien 3"
                    :postnummer "2060"}
                   :navn "Yumsies Oslo Lufthavn Gardermoen"}
                  (sut/finn-senter (system/get-state ctx))
                  :senter/id))
           "oslo-lufthavn"))))
```

Så jeg kunne ha stoppet allerede i punkt 1, men da hadde jeg ikke hatt noe
eksempel til punkt 2. Det var faktisk vanskelig å finne et eksempel hvor vi ikke
hadde tester. Sånn går no' dagan.

## 3. Snitch

Hvis du syns det er litt smågrisete å slenge inn en `def` midt i en kodesnutt,
så er jeg vel tilbøyelig til å være enig med deg. Lintern' min også. Med
[snitch](https://github.com/AbhinavOmprakash/snitch) blir det litt mer ålreit.
Den installerer en makro `defn*` som gjør det for deg:

```clj
(defn* finn-senter [state serveringssted]
  (when-let [postnummer (:postnummer (:adresse serveringssted))]
    (let [sentere (senter/finn-sentere-i-kommune state postnummer)]
      (or (adresser/finn-senter-fra-adresse sentere (:adresse serveringssted))
          (senter/finn-senter-fra-navn sentere [(:navn serveringssted)])))))
```

Som du ser har jeg byttet ut `defn` med `defn*`. Neste gang koden kjører, så
blir `state` og `serveringssted` tilgjengelige som verdier man kan titte på. Men
ikke bare det: Den vil også gjøre `postnummer` og `sentere` tilgjengelig, siden
de også `let`'es i funksjonen.

## 4. Scope Capture

Det finnes også [Scope Capture](https://github.com/vvvvalvalval/scope-capture)
som gir deg enda mer kontroll over hva slags verdier du vil fange inn i REPLet.
Jeg kommer ikke til å gå inn i alle mulighetene der, men på sitt enkleste så ser
det sånn ut:

```clj
(defn finn-senter [state serveringssted]
  (sc.api/spy ;; <==
   (when-let [postnummer (:postnummer (:adresse serveringssted))]
     (let [sentere (senter/finn-sentere-i-kommune state postnummer)]
       (or (adresser/finn-senter-fra-adresse sentere (:adresse serveringssted))
           (senter/finn-senter-fra-navn sentere [(:navn serveringssted)]))))))
```

Denne `sc.api/spy` spionerer på alle lokale variable og tar vare på dataene hver
gang den kjøres. Man kan så hente dem ut igjen med:

```clj
(sc.api/defsc 7)
```

Hvor `7` her ber om å få verdiene slik de var i den sjuende invokasjonen.

## Det er faktisk data

Her er det viktigste poenget, egentlig:

I Clojure opererer vi med data i form av lister, maps, sett, strenger, keywords
og tall. Disse kan inspiseres - *ses på* - ut av boksen. Hvis jeg får tak i
parameterne til funksjonen på noe vis, så kan jeg se på dem. Det er ikke
`[Object object]` eller `my.class.Foo@100ab127`. Det er de faktiske dataene.

Disse dataene kan tittes på med øyne, ja, men de kan også puttes inn i en
tekstfil. Sendes på mail. Pastes inn i Slack. Og så kan man evaluere dem på nytt
senere. Jeg kan for eksempel ta dataene og sende til Christian, så han kan prøve
dem på sin maskin.

Det er det som gjør det så lett å jobbe på denne måten. Jeg trenger ikke finne
ut av en constructor. Jeg trenger ikke krysse fingrene for en bra
`toString`-metode. Jeg kan når som helst titte på dataene som flyter gjennom
systemet mitt. Da blir alt så mye lettere. Inkludert å finne ut av hvilke data
som kommer her.
