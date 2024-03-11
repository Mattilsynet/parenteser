:page/title Virtuell DOM fra bunnen av
:blog-post/author {:person/id :person/christian}
:blog-post/published #time/ldt "2024-03-12T09:00:00"
:blog-post/tags [:framsideutvikling :javascript]
:blog-post/description

React og mange andre frontend-rammeverk er bygget på teknologi kjent som
"virtual DOM". I dette innlegget bygger jeg en virtuell DOM fra bunnen av for å
forstå hva det egentlig er for noe.

:open-graph/description

React er basert virtuell DOM. Men hva er det? Det finner vi ut ved å kode det
fra skrætsj.

:blog-post/body

React og mange andre frontendrammeverk er basert på "virtual DOM". Det er mindre
magisk enn du kanskje skulle tro, så la oss forsøke å implementere det selv.

## JSX

For å unngå at dette innlegget skal bli uendelig langt må vi hoppe bukk over
JSX. Det eneste vi trenger å vite er at dette:

```js
export function Heading(text) {
  return (
   <h1 className="heading">{text}</div>
  );
}
```

Blir til dette:

```js
export function Heading(text) {
  return React.createElement(
    "h1",
    {className: "heading"},
    text
  );
}
```

## Hva er egentlig virtuell DOM?

"Virtuell DOM" er en datastruktur som beskriver DOM-en, og en algoritme som
oppdaterer den faktiske DOM-en til å bli lik datastrukturen. Hele poenget med
denne indireksjonen er at det er (ganske mye) raskere å sammenligne to
JavaScript-objekter enn å sammenligne to DOM-elementer.

For å implementere en virtuell DOM trenger vi tre ingredienser:

1. En beskrivelse av ønsket DOM-struktur
2. En beskrivelse av DOM-en slik den er nå (resultatet av forrige oppdatering)
3. En funksjon som sammenligner disse og gjør nødvendige endringer i DOM-en

Altså:

```js
function render(el, vdomNew, vdomOld) {
  // Smarte greier
}
```

Funksjonen som oppretter virtuell DOM returnerer bare data:

```js
function createElement(tag, attrs, ...children) {
  return {
    tag,
    attrs,
    children
  }
}
```

## Første render-kall

Første gang vi rendrer noe er `vdomOld` rett og slett `null`, og funksjonen vår
skal bare opprette elementene som er beskrevet av `vdomNew`:

```js
function render(el, vdomNew, vdomOld) {
  el.appendChild(createNode(vdomNew));
  return vdomNew;
}
```

Funksjonen returnerer den nyeste representasjonen av DOM-en, slik at den kan
spares til neste kall.

Noden som skal opprettes kan enten være en tekstnode, eller et element.
`createNode` ser dermed sånn ut:

```js
function createNode(vdom) {
  if (typeof vdom === "string") {
    return document.createTextNode(vdom);
  } else {
    var node = document.createElement(vdom.tag);

    Object.keys(vdom.attrs).forEach(k => {
      node.setAttribute(k, vdom.attrs[k]);
    });

    vdom.children.forEach(c => {
      node.appendChild(createNode(c));
    });

    return node;
  }
}
```

## Andre render-kall

Ok, så har vi en indirekte måte å opprette DOM-noder på. Den virkelige nøtta er
å oppdatere DOM-en. Altså, når jeg gjør dette:

```js
var vdom1 = render(
  el,
  createElement("h1", {class: "heading"}, "Hei verden!"),
  null
);

var vdom2 = render(
  el,
  createElement("h1", {class: "heading"}, "Yo!"),
  vdom1
);
```

Så forventer jeg at det kun er én heading på siden, og at den viser den
oppdaterte teksten. Da må vi brette opp ermene, sammenligne de to vdom-ene og
oppdatere DOM-en deretter.

Vi trenger en ny funksjon som oppdaterer én node om gangen. Den må da ha samme
informasjon som tidligere, men også hvilken indeks vi er på (hvilket barn vi
jobber på nå). Vi trenger den indeksen for å bytte ut noden, fjerne den, eller
legge til en ny.

```js
function render(el, vdomNew, vdomOld) {
  updateDOM(el, vdomNew, vdomOld, 0);
  return vdomNew;
}
```

### Opprett ny node

Vi kan starte med caset fra tidligere: det er ingen `vdomOld`. Det betyr at det
skal lages og settes inn en ny node:

```js
function updateDOM(parent, vdomNew, vdomOld, idx) {
  if (!vdomOld) {
    insertNode(parent, createNode(vdomNew), idx)
  }
}
```

`insertNode` må ta et valg for oss: Dersom det allerede er barnenoder i `parent`
så kan vi bruke `insertBefore` for å sette det nye barnet på rett plass. Hvis
ikke må vi bruke `appendChild`:

```js
function insertNode(parent, child, idx) {
  var sibling = parent.childNodes[idx];
  if (sibling) {
    parent.insertBefore(child, sibling);
  } else {
    parent.appendChild(child);
  }
}
```

### Sammenlign strenger

Neste case er at `vdomNew` er en streng. Hvis den er lik `vdomOld` trenger vi
ikke å gjøre noe. Hvis de er forskjellige må vi bytte ut den gamle tekstnoden
med en ny:

```js
function updateDOM(parent, vdomNew, vdomOld, idx) {
  if (!vdomOld) {
    insertNode(parent, createNode(vdomNew), idx)
  } else if (typeof vdomNew === "string") {
    if (vdomNew !== vdomOld) {
      replaceNode(parent, createNode(vdomNew), idx);
    }
  }
}
```

`replaceNode` må gjøre samme sjekk som `insertNode` over, bare at den heller
bruker `replaceChild` når det er en node der fra før:

```js
function replaceNode(parent, child, idx) {
  var sibling = parent.childNodes[idx];
  if (sibling) {
    parent.replaceChild(child, sibling);
  } else {
    parent.appendChild(child);
  }
}
```

### Endre eksisterende node

Så øker vanskelighetsgraden litt. `vdomNew` og `vdomOld` representerer begge
samme node (denne antagelsen må justeres senere), så vi må finne ut av hva som
har endret seg. Det betyr:

1. Ta bort attributter som er i `vdomOld`, men ikke er i `vdomNew`
2. Sett alle attributtene som er i `vdomNew`
3. Gå gjennom alle barne-nodene og oppdater dem

`updateDOM` får da en ny case:

```js
function updateDOM(parent, vdomNew, vdomOld, idx) {
  if (!vdomOld) {
    insertNode(parent, createNode(vdomNew), idx)
  } else if (typeof vdomNew === "string") {
    if (vdomNew !== vdomOld) {
      replaceNode(parent, createNode(vdomNew), idx);
    }
  } else {
    updateNode(parent.childNodes[idx], vdomNew, vdomOld);
  }
}
```

Og `updateNode` ser foreløpig sånn ut:

```js
function updateNode(node, vdomNew, vdomOld) {
  Object.keys(vdomNew.attrs)
    .concat(Object.keys(vdomOld.attrs))
    .forEach(key => {
      if (vdomNew.attrs[key]) {
        node.setAttribute(key, vdomNew.attrs[key]);
      } else {
        node.removeAttribute(key);
      }
    });

  vdomNew.children.forEach((child, idx) => {
    updateDOM(node, child, vdomOld.children[idx], idx);
  });
}
```

## Sletting av gamle noder

Koden vår tar ikke høyde for at noder i `vdomOld` ikke lenger eksisterer i
`vdomNew`. Hvis vi gjør dette:

```js
var vdom1 = render(
  el,
  createElement(
    "h1",
    {class: "heading"},
    "Hei ", createElement("strong", {}, "verden!")),
  null
);

var vdom2 = render(
  el,
  createElement(
    "h1",
    {class: "heading"},
    "Hei verden!"),
  vdom1
);
```

Så ender vi opp med `"Hei verden!verden!"` på skjermen, den siste "verden!" i en
`strong`. Sånn kan det ikke være. Dersom det er flere barn i `vdomOld` enn
`vdomNew` så må de overskytende fjernes:

```js
function updateNode(node, vdomNew, vdomOld) {
  // Oppdater attributter
  // Oppdater barn
  // ...

  for (var i = vdomNew.children.length; i < vdomOld.children.length; i++) {
    node.removeChild(node.childNodes[i]);
  }
}
```

## Endret element-type

Koden vår tar heller ikke høyde for at `h1`-en på et tidspunkt blir bytta ut med
feks en `p`. Det er ikke mulig å bytte tagnavn på en DOM-node, så da må vi bytte
ut hele noden. Dette kan vi snike inn i `updateDOM`:

```js
function updateDOM(parent, vdomNew, vdomOld, idx) {
  if (!vdomOld) {
    insertNode(parent, createNode(vdomNew), idx)
  } else if (typeof vdomNew === "string") {
    if (vdomNew !== vdomOld) {
      replaceNode(parent, createNode(vdomNew), idx);
    }
  } else {
    if (vdomNew.tag === vdomOld.tag) {
      updateNode(parent.childNodes[idx], vdomNew, vdomOld);
    } else {
      replaceNode(parent, createNode(vdomNew), idx);
    }
  }
}
```

## Var det alt?

Ja, det var egentlig det. Herfra og ut er det kun optimaliseringer og detaljer
som gjenstår. Eksempler på detaljer er:

- Sette eventhandlere med `addEventListener` og `removeEventListener`
- Sette styles med `node.style[k] = v`
- Opprette SVG-elementer med riktig namespace
- Spesialhåndtere elementer med `contenteditable` og `innerHTML`

En viktig optimalisering som er utelatt er å oppdage at en node har flyttet seg.
Det er mye billigere å flytte en node enn å slette den for så å gjenskape den --
for ikke å snakke om at det kan trigge CSS-transisjoner helt feil.

Men i bunn og grunn er [disse 84 linjene med
JavaScript](https://gist.github.com/cjohansen/464027efa8d24d7cd8508817803fb252)
kjernen i hva som foregår når React oppdaterer DOM-en for deg. Du skal kanskje
ikke [lage ditt eget virtuell
DOM-bibliotek](https://github.com/cjohansen/replicant), men det er alltid fint å
vite ca hva som skjer under panseret på verktøyene vi bruker, og nå vet du
kanskje litt mer om hva som foregår i den virtuelle DOM-en.
