:page/title Data modeling is UX design
:page/locale :en
:blog-post/author {:person/id :person/christian}
:blog-post/published #time/ldt "2024-04-30T09:00:00"
:blog-post/tags [:modellering :ux]
:open-graph/image /images/strekkode.png
:blog-post/description

It's easy to think that usability is the responsibility of designers and
frontend folks, but many of the choices we make in the data model and system
architecture also affect how pleasant (or unpleasant) it is to use the systems
we build.

:open-graph/description

Some thoughts on how the choices we make in our data models and systems
architecture affect the usability of the systems we build -- with a focus on
unique ids.

:blog-post/body

It's easy to think that usability is the responsibility of designers and
frontend folks, but many of the choices we make in the data model and system
architecture also affect how pleasant (or unpleasant) it is to use the systems
we build.

Every system needs to address its data with IDs. These are important both
internally within the system and externally. In web apps, IDs tend to appear in
URLs and other places in the user interface. In business systems, they may even
become part of the vocabulary of the people who use them.

URLs in particular are worth some consideration. Where will these appear? On
posters? In TV commercials? Sent via email and/or SMS? The moment you want to
publish a link on Facebook, you might regret choosing UUIDs to address your
content.

But what makes a good ID? Let's look at some alternatives.

## Natural IDs

Many objects have a natural identifier. Business entities have unique
organization numbers, a person has a social security number, and even cows (at
least in Norway) have individual numbers.

Natural IDs are poor candidates for addressing because they are not owned and
controlled by the system. Some of the mentioned ids can change for political
reasons that are completely outside of our control.

## Numerical database IDs

Sequential numeric IDs from a database are another example of IDs that you
strictly do not control and should be used reluctantly outside of your system.

## UUIDs

UUIDs have become very popular in recent years. UUIDs are easy for developers to
generate without any state. When things are easy for a developer, you know it's
going to show up everywhere. The problem with UUIDs is that they are incredibly
user-unfriendly. Just imagine if this blog post lived at the following URL:

```
https://parenteser.mattilsynet.io/blogg/58ebdbab-27ac-4b77-8056-1d3c6ee87e9b/
```

Good luck reading this URL to your grandfather over the phone.

## Slugs

A "slug" is a URL-friendly version of a name or some such. These have been
popular as "URL IDs". Unfortunately, they are not very usable as IDs, as they
mostly represent volatile content (a name that may change). If you're going to
use slugs, they should be generated once and never changed.

The blog post you're currently reading has a handmade "slug" as a URL. It works
well for handmade content but scales poorly as a mechanism for general
addressing.

## System-Unique IDs

A better alternative is to create IDs that are unique within the system's scope.
By narrowing it down from "universally unique" (UUID) to "system unique," we
don't need as much entropy and can get away with much friendlier IDs.

For example, an ID could consist of 6 characters, a mix of numbers and letters.
With all the numbers and the Norwegian alphabet, you have 39^6 IDs to choose
from - over 35 billion. That should cover most needs.

A 6-character ID is obviously easier to handle than a UUID. But can we do
better? Well, some characters are more troublesome than others. Look at this
example:

> 0l9234

The first two characters -- is it `o` or `0`? `l`, capital `I`, or `1`?
Depending on the font and size, these can be very difficult to distinguish. We
can increase readability by avoiding characters that are difficult to
distinguish from each other.

`0`, `1`, `o`, and `l` are out. In addition, we can drop `æ`, `ø`, and `å`, as
they tend to cause trouble in places where IDs roam. `b`, `d`, `p`, and `t`
sound very similar, as do `m` and `n`. We also skip `v` and `y`.

That leaves us with this alphabet:

```
23456789acefghjkqrsuwxz
```

A meager 23 characters, but with only 6 of them, we can still create 150 million
IDs. With 7, you have almost 2 billion IDs.

## Make the right thing the easy choice

You might wonder why we can't just create nice URLs for content exposed in the
interface?

By ensuring that the system's IDs are readable and user-friendly in themselves,
we have made it almost impossible for developers to push obscure sequences of
numbers and letters into users' faces.

It is still possible to create handmade slugs on URLs where appropriate, but
even if we forget to do so, we fall back on a form of addressing that is
human-friendly.

Developers also benefit from these IDs when digging into data, following up on
issues in our monitoring, and interacting with the database. And users get URLs
and IDs that they can convey orally and write correctly on the first try.
Everyone wins.

For those especially interested, I have outlined [a concrete implementation in a
separate post](/en/user-friendly-ids-clojure/).
