:page/title En samtale mellom NATS og Clojure
:blog-post/author {:person/id :person/christian}
:blog-post/published #time/ldt "2024-05-15T09:00:00"
:blog-post/tags [:clojure :nats]
:blog-post/series {:series/id :nats}
:open-graph/image /images/nats.png
:blog-post/description

Det finnes foreløpig ikke noe offisielt NATS-bibliotek for Clojure, så hvordan i
huleste får vi parentes-frelste emnebasert meldingsutveksling inn i systemene
våre?

:open-graph/description

Om å snakke med NATS fra Clojure, selv uten et offisielt klientbibliotek.

:blog-post/body

[NATS](/intro-til-nats/) er en slagkraftig meldingsserver med
[mange](/nats-jet-stream/) [attraktive](/nats-kv/) bruksområder. Dessverre
finnes det enda ikke noe klient-bibliotek for Clojure, så hvordan kan vi
fortsette å skrive systemene våre i Clojure **og** ta i bruk NATS?

## Interop

Clojure har ikke sitt eget kjøretidsmiljø, men lever i en herlig symbiose med
vertsspråket sitt. Det er bygget for å kjøre på eksisterende plattformer, og å
integrere tett med dem. De to mest ubredte variantene er Clojure som kjører på
JVM-en, og ClojureScript som kjører der JavaScript kjører.

Clojure har førsteklasses støtte for interoperabilitet med plattformen det
kjører på. I praksis betyr det at det er fullt mulig å skrive Java med
Clojure-syntaks og få det til å kjøre. Alt som er tilgjengelig for Java
(inkludert hele Maven central) kan brukes fra Clojure.

### jnats

Når det ikke finnes dedikerte verktøy for Clojure ser vi til plattformen, og
visst finnes det et klientbibliotek for Java,
[jnats](https://javadoc.io/doc/io.nats/jnats/latest/index.html). Med Java ser
det sånn ut:

```java
package nats.example;

import io.nats.client.Connection;
import io.nats.client.Message;
import io.nats.client.Nats;
import io.nats.client.Subscription;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

public class Demo {
  public static void main(String[] args) {
    // Emne
    String subject = "chat.general";

    // Koble til serveren
    Connection nc = Nats.connect("nats://localhost:4222");

    // Vent på en melding
    new Thread(() -> {
        Subscription sub = nc.subscribe(subject);
        Message msg = sub.nextMessage(Duration.ofSeconds(1));

        System.out.printf("Fikk melding \"%s\" på emne \"%s\"\n",
                  new String(msg.getData(), StandardCharsets.UTF_8),
                  msg.getSubject());
    }).start();

    // Publiser en melding
    String message = "Hello world!";
    nc.publish(subject, message.getBytes(StandardCharsets.UTF_8));
  }
}
```

Dette kan vi oversette til Clojure mer eller mindre linje for linje.

Java har pakker, Clojure har namespaces. Vi starter med å definere namespacet og
importere avhengighetene vi trenger:

```clj
(ns nats.example
  (:import (io.nats.client Connection Message Nats Subscription)
           (java.nio.charset StandardCharsets)
           (java.time Duration)))
```

Vi trenger hverken klassen eller main-metoden for dette eksempelet, så vi går
rett til å opprette en connection og definere emnet:

```clj
(def conn (Nats/connect "nats://localhost:4222"))
(def subject "chat.general")
```

Så lytter vi på en melding. Clojure-funksjoner er `Runnable` og kan dermed ta
plassen til lambda-syntaksen:

```clj
(.start
 (Thread.
  (fn []
    (let [sub (.subscribe conn subject)
          msg (.nextMessage sub (Duration/ofSeconds 1))]
      (println
       (format "Fikk melding \"%s\" på emne \"%s\"\n"
               (String. (.getData msg) StandardCharsets/UTF_8)
               (.getSubject msg)))))))
```

Parentesene flytter seg på motsatt side og vi får litt nøsting, men ellers er
parallellen til Java-koden nokså synlig. Til slutt publiserer vi en melding:

```clj
;; Publiser en melding
(.publish conn subject (.getBytes "Hello world!" StandardCharsets/UTF_8))
```

For ordens skyld, her er hele kodesnutten:

```clj
(ns nats.example
  (:import (io.nats.client Connection Message Nats Subscription)
           (java.nio.charset StandardCharsets)
           (java.time Duration)))

;; Koble til serveren
(def conn (Nats/connect "nats://localhost:4222"))
(def subject "chat.general")

;; Vent på en melding
(.start
 (Thread.
  (fn []
    (let [sub (.subscribe conn subject)
          msg (.nextMessage sub (Duration/ofSeconds 1))]
      (println
       (format "Fikk melding \"%s\" på emne \"%s\"\n"
               (String. (.getData msg) StandardCharsets/UTF_8)
               (.getSubject msg)))))))

;; Publiser en melding
(.publish conn subject (.getBytes "Hello world!" StandardCharsets/UTF_8))
```

At dette fungerer betyr at du kan utforske Java-biblioteker med Clojure. Jeg vil
til og med påstå at Clojure er den beste måten å gjøre akkurat det på, nettopp
fordi du kan sitte i [REPL-et](https://www.kodemaker.no/blogg/2022-10-repl/) og
prøve deg frem.

## Problemet løst?

Ok, så Clojure kan snakke Java med NATS, da er vel problemet løst? Teknisk sett,
ja. Men jnats er "så Java som du får det", og koden blir nokså klossete og lite
idiomatisk. En av de store fordelene med Clojure er de innebyggede datatypene,
og til de får du ingen hjelp av jnats - den opererer kun med bytes.

Se bare hva som skjer i det øyeblikket du trenger å sende inn parametere -- de
må inn som et objekt. Hvordan oppretter du det? Med et builder-objekt,
selvfølgelig:

```clj
(ns nats.example
  (:import (io.nats.client Connection JetStreamOptions Nats)
           (io.nats.client.api RetentionPolicy StreamConfiguration)
           (java.time Duration)))

(def conn (Nats/connect "nats://localhost:4222"))

(def jet-stream
  (let [options (-> (JetStreamOptions/builder)
                    (.requestTimeout (Duration/ofMillis 1000))
                    .build)]
    (.jetStreamManagement conn options)))

(let [options (-> (StreamConfiguration/builder)
                  (.name "chats")
                  (.description "Chat-meldinger")
                  (.subjects (into-array String ["chat.>"]))
                  (.retentionPolicy RetentionPolicy/Limits)
                  (.maxAge (Duration/ofDays 30))
                  .build)]
  (.addStream jet-stream options))
```

Dette er bare for å opprette en strøm. De små smulene av mine data drukner i
detaljene til jnats-API-et. For ikke å snakke om at retur-verdien fra
`.addStream` er et opakt objekt, ikke data, slik jeg er vant til:

```java
#object[io.nats.client.api.StreamInfo 0xe86de3a "StreamInfo {...}"]
```

Hvis jeg nå vil publisere en melding med headere må jeg til med en
`MessageBuilder`, og det blir mer støy.

Så ja, interoperabilitet gjør ting tilgjengelig, men mange Java API-er er
designet med en helt annen sensibilitet enn Clojure-kode. `java.time` er et
hederlig unntak her, det bruker jeg alltid som det er. jnats er helt i andre
enden, har stor kontaktflate og tar stor plass i koden.

## Pakk det inn

Så hva gjør vi med kode som stinker litt? Vi pakker den inn, og unngår at rotet
sprer seg overalt. For eksempel kunne vi laget en funksjon for å opprette en
strøm som skjuler litt detaljer og konverterer resultatet til data:

```clj
(create-stream conn
 {:request-timeout 1000
  :stream-name "chats"
  :description "Chat-meldinger"
  :subjects ["chat.>"]
  :retention-policy RetentionPolicy/Limits
  :max-age (Duration/ofDays 30)})
```

Dette funksjonskallet er deilig fritt for støyende teknikaliteter: opprett en
strøm med disse parameterne.

Siden jnats er omfattende og det er mye mapping som skal til har jeg bestemt meg
for å gjøre den jobben en gang for alle og tilby det som et bibliotek. Og sånn
ble [clj-nats](https://github.com/cjohansen/clj-nats) til.

## NATS fra Clojure, på Clojure-vis

clj-nats er for det meste litt padding rundt jnats som gjør det mer ergonomisk å
jobbe med fra Clojure. En viktig del av dette er å gjøre det lett å jobbe med
Clojure sine datastrukturer.

En NATS-melding er bare noen bytes -- det er opp til klienten å vite hvordan
disse skal tolkes. Heldigvis kan en NATS-melding også ha headere, lignende en
HTTP-forespørsel. Dette bruker clj-nats til å transparent serialisere og
deserialisere meldinger til og fra EDN:

```clj
(require '[nats.core :as nats])

(def conn (nats/connect "nats://localhost:4222"))

(nats/publish conn
  {:nats.message/subject "chat.general.christian"
   :nats.message/data {:message "Hello world!"}})
```

Hvis du lytter på meldinger med kommandolinjeklienten ser du følgende:

```sh
nats subscribe '>'

[#3300587] Received on "chat.general.christian"
content-type: application/edn

{:message "Hello world!"}
```

Så der har du det. Vi parentesfrelste kan snakke med NATS via Java-interop, og
nå også via et eget Clojure-bibliotek, som snakker Clojure sine datastrukturer.
[Readme-en til clj-nats](https://github.com/cjohansen/clj-nats) har mange flere
eksempler.
