:page/title clojure.core/keep forklart med monader
:blog-post/author {:person/id :person/teodor}
:blog-post/published #time/ldt "2025-09-16T12:56:31.413378"
:blog-post/tags [:haskell :clojure :funksjonell-programmering]
:blog-post/description

`clojure.core/keep` er overraskende monadisk.
Bli med på en reise i typer.

:blog-post/body

*Monader*, kanskje det dårligst forklarte og mest forvirrende computer science-begrepet på internett!
"Det er en burrito", hva pokker?
("Monader er burritoer" er en faktisk forklaring folk har prøvd seg på, prøv et google-søk).

Og hva i all verden har monader med `keep` fra `clojure.core` å gjøre?
Dagens tekst tar deg gjennom litt "type først"-tankegods, før vi returnerer til Clojure for å få eksempler i REPL:

1. Lynkurs i lesing av Haskell-typer
2. Monader, forklart med Haskell-typer
3. Monadiske operasjoner med Clojure-data
4. Til slutt, hva er egentlig `clojure.core/keep`?

## Lynkurs i Haskell-typer

Før vi kan forklare monader med typer, må vi trene på å lese typer.
Og for å være presise, snakker vi om monader *som omtalt i Haskell-økosystemet*.

I Haskell putter du typen bak `::`:

```haskell
age :: Int
age = 3
```

Funksjoner av ett argument får en pil (`->`) i signaturen sin.

```haskell
increase :: Int -> Int
increase x = x + 1
```

Funksjoner av to eller flere argumenter får to eller flere piler i signaturen.
Det har en kul teknisk forklaring som er langt utenfor fokuset til denne teksten[¹](#fot1).

```haskell
average :: Double -> Double -> Double
average x y = (x + y) / 2
```

Lister i Haskell likner mistenkelig på JSON-arrays:

```haskell
imdbRatings :: [Double]
imdbRatings = [8.2, 9.1, 8.4]
-- bonus-spørsmål: hvilke tre filmer er dette fra?
```

... og i stedet for en magisk verdi som er ingenting, men er av alle typer, er
"verdi som kanskje er tom" en eksplisitt type:

```haskell
envisioningInformationAuthor :: Maybe String
envisioningInformationAuthor = Just "Edvard Tufte"

bhagavadGitaAuthor :: Maybe String
bhagavadGitaAuthor = Nothing

-- de fleste bøker er skrevet av en forfatter, men det er ikke
-- alltid like lett å peke til én person.
```

Sånn!
Da kan vi nok om Haskell-typer til å forklare monader.

## Typesignatur for API-kontrakten "monade"

En monade er en API-kontrakt for en type.
API-kontrakten krever at du implementerer to funksjoner.

Den første funksjonen er `return`.
`return` skal returnere en tom instans av den monadiske typen.
La oss implementere den for lister og maybe.

```haskell
returnList :: [a]
returnList = []

returnMaybe :: Maybe a
returnMaybe = Nothing
```

Hvis du lurer på hvor `a` kommer fra, er dette en ubrukt type, som vi heller ikke går inn på i dag.

Den andre funksjonen kalles `bind` (og har sin egen infix-operator, `>>=`, som vi også ignorerer i dag).
Dette er typen til bind:

```haskell
ghci> :t (>>=)
(>>=) :: Monad m => m a -> (a -> m b) -> m b
```

Her får vi *to* ubrukte typer, `m` og `a`.
Vi kan få vekk `m` ved å spesialisere bind til lister og maybe.

```haskell
bindList :: [a] -> (a -> [b]) -> [b]
bindList = bind

bindMaybe :: Maybe a -> (a -> Maybe b) -> Maybe b
bindMaybe = bind
```

## Eksempler på API-kontrakten "monade"

Jeg foretrekker forklaringer med eksempler, da får jeg noe håndfast å forholde meg til.
Maybe og liste er konkrete monader.
Nå skal vi også få konkrete eksempler på *bruk* av monade-operasjonen.

`return` er lett:

```haskell
ghci> returnList
[]
ghci> returnMaybe
Nothing
```

Tom liste for lister og en "ingenting" for Maybe.

Bind er gjør noe mer spennende.
Typen til bind avdekker at ett av argumentene er en funksjon.

```haskell
ghci> :t bindList
bindList :: [a] -> (a -> [b]) -> [b]
--                 ^
--                 funksjon fra a til liste av b

```

"fra ett element til liste av mange elementer"?
Hmm.

Når trenger vi det, mon tro?

```haskell
adjectivize :: String -> [String]
adjectivize s = map (\a -> a ++ " " ++ s) ["The fabulous", "The ingenious", "The completely dreamy-eyed"]
```

Du kan ignorere implementasjonen til `adjectivize` hvis du vil, essensen ligger i typesignaturen.

```haskell
ghci> adjectivize "Arne"
["The fabulous Arne","The ingenious Arne","The completely dreamy-eyed Arne"]
```

Når vi adjektiviserer flere navn, passer signaturen til `bindList`!

```haskell
ghci> bindList ["Arne", "Tom", "Tim", "John"] adjectivize
["The fabulous Arne","The ingenious Arne","The completely dreamy-eyed Arne","The fabulous Tom","The ingenious Tom","The completely dreamy-eyed Tom","The fabulous Tim","The ingenious Tim","The completely dreamy-eyed Tim","The fabulous John","The ingenious John","The completely dreamy-eyed John"]
```

Bred linje, sorry.
Jeg jukser litt (med hemmelige, ikke-forklarte funksjoner) så vi kan se resultatet.

```haskell
ghci> mapM_ print $ bindList ["Arne", "Tom", "Tim", "John"] adjectivize
"The fabulous Arne"
"The ingenious Arne"
"The completely dreamy-eyed Arne"
"The fabulous Tom"
"The ingenious Tom"
"The completely dreamy-eyed Tom"
"The fabulous Tim"
"The ingenious Tim"
"The completely dreamy-eyed Tim"
"The fabulous John"
"The ingenious John"
"The completely dreamy-eyed John"
```

Lekse:

> bindList lar oss "flate ned" to lag med lister.

Maybe-monaden er nyttig for eksempel på heltallsdivisjon med eksplisitte avrundingsfeil.

```haskell
safeHalf :: Int -> Maybe Int
safeHalf x = let guess = div x 2
             in if guess * 2 == x
                then Just guess
                else Nothing
```

```haskell
ghci> safeHalf 10
Just 5
ghci> safeHalf 9
Nothing
```

Vi kan nå gjøre mange heltallsdivisjoner uten eksplosjon av if-else-mikmakk i koden:

```haskell
ghci> bindMaybe (safeHalf 100) safeHalf
Just 25
```

Bind som infix-operator lar oss i tillegg unngå eksplosjon av parenteser.

```
-- med infix >>= kan vi slenge på mer jobb på slutten:
ghci> (safeHalf 1000) >>= safeHalf >>= safeHalf >>= safeHalf
Nothing
ghci> (safeHalf 2000) >>= safeHalf >>= safeHalf >>= safeHalf
Just 125

-- ... i kontrast til
ghci> bindMaybe (bindMaybe ( bindMaybe (safeHalf 2000) safeHalf ) safeHalf ) safeHalf
Just 125
```

Lekse:

> bindMaybe lar oss "flate ned" to lag med "kanskje-verdier".

## `mapcat` er `bindList`

Nok Haskell for nå?
Rich to the Rescue.

```clojure
(defn adjectivize [s]
  (map #(str % " " s)
       ["The fabulous", "The ingenious", "The completely dreamy-eyed"]))

(adjectivize "Arne")
;; => ("The fabulous Arne" "The ingenious Arne" "The completely dreamy-eyed Arne")

(mapcat adjectivize ["Arne", "Tom", "Tim", "John"])
;; => ("The fabulous Arne"
;;     "The ingenious Arne"
;;     "The completely dreamy-eyed Arne"
;;     "The fabulous Tom"
;;     "The ingenious Tom"
;;     "The completely dreamy-eyed Tom"
;;     "The fabulous Tim"
;;     "The ingenious Tim"
;;     "The completely dreamy-eyed Tim"
;;     "The fabulous John"
;;     "The ingenious John"
;;     "The completely dreamy-eyed John")
```

Hadde du puttet en Haskell-typesignatur på `mapcat` hadde du fått nettopp typen `[a] -> (a -> [b]) -> [b]`.

## `some->` er nesten `bindMaybe`

Når vi gjør trygg halvering i Clojure, bruker vi `nil` ved manglende verdi.

```clojure
(defn safe-half [x]
  (let [guess (quot x 2)]
    (when (= x (* guess 2))
      guess)))

(safe-half 10)
;; => 5

(safe-half 9)
;; => nil
```

Vi bruker `some->` som kombinator for `safe-half`:

```clojure
(some-> 100 safe-half safe-half)
;; => 25
```

... og hvis vi vil kjøre "mange runder", slipper vi å gjenta en infiks-operator.

```clojure
(some-> 1000 safe-half safe-half safe-half safe-half)
;; => nil

(some-> 2000 safe-half safe-half safe-half safe-half)
;; => 125
```

## Kan liste-monadisk og maybe-monadisk oppførsel blandes?

Hvis vi skal dele en liste trygt på to, ender vi opp med en liste av maybe-verdier.

```haskell
ghci> map safeHalf [0..20]
[Just 0,Nothing,Just 1,Nothing,Just 2,Nothing,Just 3,Nothing,Just 4,Nothing,Just 5,Nothing,Just 6,Nothing,Just 7,Nothing,Just 8,Nothing,Just 9,Nothing,Just 10]
```

Fordi vi blander lister og maybe, kan vi verken bruke bindList eller bindMaybe!

Men la oss ikke gi opp.

```haskell
maybeToList :: Maybe a -> [a]
maybeToList Nothing = []
maybeToList (Just x) = [x]
```

Hah, nå kan vi late som at alt er lister!
… og da går typene opp ☺️

```haskell
ghci> bindList [0..20] (maybeToList . safeHalf)
[0,1,2,3,4,5,6,7,8,9,10]
```

Dette så ut som en nyttig funksjon å ha 🧐

Vi vil ha denne typesignaturen:

```haskell
mystery :: [a] -> (a -> Maybe b) -> [b]
```

Implementasjonen har vi nesten allerede!
Vi må bare la være å hardkode `safeHalf`.

```haskell
mystery xs f = bindList xs (maybeToList . f)
```

Da kan vi skrive om eksemplet vårt!

```haskell
ghci> mystery [0..20] safeHalf
[0,1,2,3,4,5,6,7,8,9,10]
```

🔥

## … men disse tankene har andre folk tenkt før …

`mystery`-funksjonen finnes såklart allerede i Haskell.
Ved å søke opp [typesignaturen i Hoogle](https://hoogle.haskell.org/?hoogle=%5Ba%5D%20-%3E%20(a%20-%3E%20Maybe%20b)%20-%3E%20%5Bb%5D),
finner vi `Data.Maybe.mapMaybe`.

```haskell
ghci> import Data.Maybe as M
ghci> :t M.mapMaybe
M.mapMaybe :: (a -> Maybe b) -> [a] -> [b]
```

Argumentene er byttet om så funksjonen kommer først, men ellers er den lik vår `mystery`.

```haskell
ghci> Data.Maybe.mapMaybe safeHalf [0..20]
[0,1,2,3,4,5,6,7,8,9,10]
```

... og i Clojure har vi reimplementert `keep`.

```clojure
(keep safe-half (range 0 (inc 20)))
;; => (0 1 2 3 4 5 6 7 8 9 10)
```

## Lærepenger

- Kanskje-verdier og lister er supre å jobbe med i både Clojure og Haskell.
  Vi kan si mye med lite kode.

- Haskell oppfordrer til å tenke i typer, Clojure oppfordrer til å tenke i eksempler.
  Bruke begge.

- Når et prinsipp er for abstrakt, spespesialiser til eksempler.
  Når du har kontroll på eksemplene, løft blikket og gi prinsippene et nytt blikk.

<hr style="margin-top: 6rem; margin-bottom: 3rem"></hr>

*fotnoter*:

<a name="fot1">¹</a>:
Start for eksempel på "Higher order functions"-kapittelet i Learn You a Haskell for Great Good: https://learnyouahaskell.com/higher-order-functions#curried-functions
