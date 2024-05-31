:page/title A Conversation Between NATS and Clojure
:page/locale :en
:blog-post/author {:person/id :person/christian}
:blog-post/published #time/ldt "2024-05-15T09:00:00"
:blog-post/tags [:clojure :nats]
:blog-post/series {:series/id :nats-en}
:open-graph/image /images/nats.png
:blog-post/description

There is currently no official NATS library for Clojure, so how on earth do we,
lovers of parentheses, get subject-based messaging into our systems?

:open-graph/description

On communicating with NATS from Clojure, even without an official client library.

:blog-post/body

[NATS](/intro-til-nats/) is a powerful messaging system with
[many](/nats-jet-stream/) [attractive](/nats-kv/) use cases. Unfortunately,
there is no client library for Clojure yet, so how can we continue to write our
systems in Clojure and utilize NATS?

## Interop

Clojure doesn’t have its own runtime environment but lives in a delightful
symbiosis with its host language. It’s built to run on existing platforms and
integrate closely with them. The two most common variants are Clojure running on
the JVM and ClojureScript running wherever JavaScript runs.

Clojure has first-class support for interoperability with its platform. In
practice, this means that it’s entirely possible to write Java with Clojure
syntax and get it to run. Everything available to Java (including the entire
Maven Central) can be used from Clojure.

### jnats

When there aren’t dedicated tools for Clojure, we look to the platform, and yes,
there is a client library for Java,
[jnats](https://javadoc.io/doc/io.nats/jnats/latest/index.html). With Java, it
looks like this:

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

        System.out.printf("Received message \"%s\" on subject \"%s\"\n",
                  new String(msg.getData(), StandardCharsets.UTF_8),
                  msg.getSubject());
    }).start();

    // Publiser en melding
    String message = "Hello world!";
    nc.publish(subject, message.getBytes(StandardCharsets.UTF_8));
  }
}
```

We can translate this to Clojure more or less line by line.

Java has packages, Clojure has namespaces. We start by defining the namespace
and importing the dependencies we need:

```clj
(ns nats.example
  (:import (io.nats.client Connection Message Nats Subscription)
           (java.nio.charset StandardCharsets)
           (java.time Duration)))
```

We don’t need the class or main method for this example, so we go straight to
creating a connection and defining the subject:

```clj
(def conn (Nats/connect "nats://localhost:4222"))
(def subject "chat.general")
```

Then we listen for a message. Clojure functions are Runnable and can thus take
the place of the lambda syntax:

```clj
(.start
 (Thread.
  (fn []
    (let [sub (.subscribe conn subject)
          msg (.nextMessage sub (Duration/ofSeconds 1))]
      (println
       (format "Received message \"%s\" on subject \"%s\"\n"
               (String. (.getData msg) StandardCharsets/UTF_8)
               (.getSubject msg)))))))
```

The parentheses move to the opposite side, and we get some nesting, but
otherwise, the parallel to the Java code is quite visible. Finally, we publish a
message:

```clj
;; Publiser en melding
(.publish conn subject (.getBytes "Hello world!" StandardCharsets/UTF_8))
```

For completeness, here is the entire code snippet:

```clj
(ns nats.example
  (:import (io.nats.client Connection Message Nats Subscription)
           (java.nio.charset StandardCharsets)
           (java.time Duration)))

;; Connect to the server
(def conn (Nats/connect "nats://localhost:4222"))
(def subject "chat.general")

;; Wait for a message
(.start
 (Thread.
  (fn []
    (let [sub (.subscribe conn subject)
          msg (.nextMessage sub (Duration/ofSeconds 1))]
      (println
       (format "Received message \"%s\" on subject \"%s\"\n"
               (String. (.getData msg) StandardCharsets/UTF_8)
               (.getSubject msg)))))))

;; Publish a message
(.publish conn subject (.getBytes "Hello world!" StandardCharsets/UTF_8))
```

The fact that this works means that you can explore Java libraries with Clojure.
I would even argue that Clojure is the best way to do just that, precisely
because you can sit in the REPL and experiment.

## Problem solved?

Okay, so Clojure can speak Java with NATS, problem solved then? Technically,
yes. But jnats is "as Java as it gets," and the code becomes quite clumsy and
unidiomatic. One of the major benefits of Clojure is the built-in immutable data
types, and jnats does not help you with those -- it only operates with bytes.

See what happens the moment you need to send in parameters -- they must go in as
an object. How do you create that? With a builder object, of course:

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

This is just to create a stream. The small crumbs of my data drown in the
details of the jnats API. Not to mention that the return value from `.addStream`
is an opaque object, not data, as I am used to:

```java
#object[io.nats.client.api.StreamInfo 0xe86de3a "StreamInfo {...}"]
```

If I now want to publish a message with headers, I need to use a
`MessageBuilder`, and it gets noisier.

So yes, interoperability makes things available, but many Java APIs are designed
with a completely different sensibility than Clojure code. java.time is a
commendable exception here, I always use it as it is. jnats is on the other end,
having a large contact surface and taking up a lot of space in the code.

## Wrap It Up

So what do we do with code that stinks a bit? We wrap it up and avoid the mess
spreading everywhere. For example, we could create a function to create a stream
that hides some details and converts the result to data:

```clj
(create-stream conn
 {:request-timeout 1000
  :stream-name "chats"
  :description "Chat-meldinger"
  :subjects ["chat.>"]
  :retention-policy RetentionPolicy/Limits
  :max-age (Duration/ofDays 30)})
```

This function call is delightfully free of noisy technicalities: create a stream
with these parameters.

Since jnats is extensive and a lot of mapping is needed, I decided to do the job
once and for all and offer it as a library. And that’s how
[clj-nats](https://github.com/cjohansen/clj-nats) came to be.

## NATS from Clojure, the Clojure Way

clj-nats is mostly a bit of padding around jnats to make it more ergonomic to
work with from Clojure. An important part of this is making it easy to work with
Clojure's data structures.

A NATS message is just some bytes -- it's up to the client to know how to
interpret these. Fortunately, a NATS message can also have headers, similar to
an HTTP request. This is used by clj-nats to transparently serialize and
deserialize messages to and from EDN:

```clj
(require '[nats.core :as nats])

(def conn (nats/connect "nats://localhost:4222"))

(nats/publish conn
  {:nats.message/subject "chat.general.christian"
   :nats.message/data {:message "Hello world!"}})
```

f you listen to messages with the command-line client, you see the following:

```sh
nats subscribe '>'

[#3300587] Received on "chat.general.christian"
content-type: application/edn

{:message "Hello world!"}
```

So there you have it. Us parentheses lovers can communicate with NATS via Java
interop, and now also through a dedicated Clojure library that speaks Clojure’s
data structures. The [README for
clj-nats](https://github.com/cjohansen/clj-nats) contains many more examples.
