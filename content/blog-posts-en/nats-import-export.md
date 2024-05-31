:page/title Queues in practice: Migrating data
:page/locale :en
:blog-post/author {:person/id :person/christian}
:blog-post/published #time/ldt "2024-05-28T09:00:00"
:blog-post/tags [:nats :clojure]
:open-graph/image /images/nats.png
:blog-post/description

I have written a lot about what [NATS](/en/nats/) is and how it works. It's
about time to talk about the most important thing: what to use it for.

:open-graph/description

On how queues can simplify and improve processes that transfer data between
systems.

:blog-post/body

Our team is building a new system for inspectors at the Norwegian Food Safety
Authority working with
[smilefjesordningen](https://www.mattilsynet.no/mat-og-drikke/forbrukere/smilefjesordningen).
The inspectors currently use an older system, and part of our job involves
migrating data from this system to the one we are building now.

The new system has a richer data model than the old one and uses a different
database, so when I say "migrate data," I actually mean exporting the usable
data, processing it, and then importing it into the new system.

## Synchronous Export/Import

The most straightforward way to migrate data from one system to another is to
write some code that reads from a database or an API, iterates through the
result set, and writes to the new database/API. This approach has several
limitations:

- The code that exports and the code that imports must be co-located.
- Unforeseen issues during the process halt the entire operation.
- You have little insight into what data is in transit (beyond what you
  remembered to log).
- Export and import must occur simultaneously.

## Asynchronous Export/Import via a Queue

We can solve all these problems by placing a queue in the middle of the process.
So how do we do that?

1. Create a queue with suitable semantics.
2. Rewrite the export to read from the source system and publish messages to the
   queue.
3. Rewrite the import to read from the queue, process the message, and import
   it.

Now, export and import are two separate processes, and the communication between
them occurs in a physical place that we can inspect to gain insight into what's
happening.

With two separate processes, the code for export and import no longer needs to
be co-located. Since the export code pertains to the old system, it has little
value in the codebase of the new system and can thus be moved out. Export and
import also do not need to run simultaneously.

"Problems along the way" can now be divided in two: problems with export or
problems with import. Since export merely reads data and puts it on a queue,
there is little that can go wrong here.

If issues arise during the import, we now have queue semantics to rely on. For
example, we can refrain from "ack"-ing the failing message so it remains on the
queue to be retried later—after we have fixed the code. We can also place the
message on a dedicated error queue.

Since communication now occurs over a queue, we have full insight into all data
being transferred, and we can even use NATS tooling to monitor how many messages
have been published, processed, and failed.

## A Queue with "Suitable Semantics"

So, what are suitable semantics in this case? We have chosen to handle import
with a work queue, in NATS terminology. Messages on a work queue disappear once
a consumer has "ack"-ed them: hence, each message can only be processed once.
This is precisely what we want for an import.

With a work queue, the export is no longer persisted after the message is
processed, but we have other mechanisms in place to save this data.

Here is what our queue configuration looks like:

```clj
(import '[clj-nats.core :as nats]
        '[clj-nats.stream :as stream])

(def conn (nats/connect "nats://localhost:4222"))

(stream/create-stream conn
  {:nats.stream/name "matnyttig-mats-import"
   :nats.stream/description "Work queue for å importere tilsynsobjekter fra MATS"
   :nats.stream/subjects #{"matnyttig.mats-import.>"}
   :nats.stream/retention-policy :nats.retention-policy/work-queue
   :nats.stream/allow-direct? true
   :nats.stream/allow-rollup? false
   :nats.stream/deny-delete? false
   :nats.stream/deny-purge? false})
```

The most important part of this configuration is:

```clj
  :nats.stream/retention-policy :nats.retention-policy/work-queue
```

This is the policy that makes NATS remove messages from the queue as they are
processed. It is also worth noting that `:nats.stream/allow-direct? true` allows
us to inspect messages on the queue without consuming them.

Our consumer looks like this:

```clj
(consumer/create-consumer conn
  {:nats.consumer/stream-name "matnyttig-mats-import"
   :nats.consumer/name "matnyttig-mats-importer"
   :nats.consumer/ack-policy :nats.ack-policy/explicit    ;; 1
   :nats.consumer/deliver-policy :nats.deliver-policy/all ;; 2
   :nats.consumer/durable? true                           ;; 3
   :nats.consumer/max-deliver 3})                         ;; 4
```

1. Explicit "ack" means we must notify NATS when the message is safely imported
   (telling NATS it can be removed from the queue).
2. We want to receive all messages since the beginning of time (not just those
   published after the consumer comes online, etc.).
3. The consumer’s state is stored on the server. If we interrupt the import, it
   will continue where it left off when it comes back online.
4. If we cannot import the message after 3 attempts, we give up.

And there you have it: By moving communication between two processes to a queue,
we have gained much better control and insight into them, while also removing
the dependency between them. "I like it a lot!"
