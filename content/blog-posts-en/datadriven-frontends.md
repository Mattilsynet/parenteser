:page/title Datadriven frontends
:page/locale :en
:blog-post/published #time/ldt "2024-01-17T09:00:00"
:blog-post/author {:person/id :person/christian}
:blog-post/tags [:framsideutvikling]
:blog-post/series {:series/id :foredrag-en}
:blog-post/description

Since I started using React over 10 years ago, my approach to frontend
development has become increasingly data-driven. At JavaZone, I showcased some
of the benefits it has brought me and why you should consider a similar
approach.

:open-graph/description

Better code with data-driven frontends. How? Why?

:blog-post/body

In this presentation, I showcase where I've ended up after a 10-year journey
with a goal of more [data-driven frontend
architecture](https://vimeo.com/861600197). I present several examples from a
codebase that, even after 10,000 commits over 9 years, is in its best shape
ever: changes are made on the fly, and features roll out faster than ever — all
thanks to a data-driven architecture.

The presentation takes you through the journey and the choices made along the
way, along with all the fruits we harvested during the journey. Enjoy!

<div class="video-responsive">
  <iframe class="video-responsive-item" src="https://player.vimeo.com/video/861600197?h=0084e31028&color=ff9933&portrait=0" allow="autoplay; fullscreen; picture-in-picture" allowfullscreen></iframe>
</div>

<br><br>

## The tools

During the presentation, I showcase some tools we've developed and used to
support the process. Here are the main ones for those who want to delve a bit
deeper:

- [Portfolio](https://github.com/cjohansen/portfolio) allows you to work
  isolated with UI components to focus on the visual aspects, similar to what
  Storybook does.
- [Dumdom](https://github.com/cjohansen/dumdom) resembles React but only
  supports props, thus pushing you towards a data-driven approach.
- [m1p](https://github.com/cjohansen/m1p) is a library that solves, among other
  things, i18n in a completely data-driven way.

Among these, Dumdom particularly opened the doors to many of our solutions. For
example, that's where we opened up for completely data-driven event handlers.

Dumdom is based on [snabbdom](https://github.com/snabbdom/snabbdom). After using
it for a few years, I realized that the rendering library can — or perhaps
should — be even simpler. Therefore, I've started on a new [virtual DOM
library](https://github.com/cjohansen/replicant) that is stripped down to the
bone. More on this in the near future.
