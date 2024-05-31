:page/title NATS key/value store
:page/locale :en
:blog-post/author {:person/id :person/christian}
:blog-post/published #time/ldt "2024-04-15T09:00:00"
:blog-post/tags [:nats :koer]
:blog-post/series {:series/id :nats-en}
:open-graph/image /images/nats.png
:blog-post/description

That a messaging system should also be able to function as a key/value database
didn't make much sense to me until I understood how it all fit together. So
let's dissect NATS' key/value store.

:open-graph/description

A walkthrough of how NATS implements its key/value store.
