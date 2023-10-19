# Parenteser - Mat-teamets blogg

Dette er bloggen til team Mat i Mattilsynet,
[parenteser.mattilsynet.io](https://parenteser.mattilsynet.io). Her tenker vi å
blogge litt om utviklingsarbeidet vårt: alt fra interessante (i våre øyne)
detaljer fra det tekniske arbeidet, hvordan vi jobber med arkitektur, til
forretningsutvikling. Time vil show, som de sier på Flåklypa TV.

## Kjør lokalt

Dersom du bruker Emacs: gjør `cider-jack-in`, åpne `parenteser.dev`, og evaluer
kallet som oppretter appen samt kallet til start-funksjonen. Bloggen er nå
tilgjengelig på [http://localhost:5052](http://localhost:5052).

## Tester

Ja, det er noen av dem også:

```sh
make test
```

## Bidrag

Fant du en skrivefeil? Eller kanskje en tungt formulert setning? Send oss gjerne
en pull request som fikser opp i [innleggene](./content/blog-posts), det blir vi
takknemlige for 🙏

Vi tar gjerne i mot fikser på funksjonelle ting som ikke virker også - eller
kanskje du har lyst til å legge til en eller annen feature som gjør
leseopplevelsen litt hyggeligere? Kjør på, og det er lov å åpne PR-en før du
skriver koden, så kan vi bli enige om at det er en god idé.

## Produksjonsmiljø og sjøsetting

Bloggen kjører i et [Docker image](./docker) med nginx og masse deilige,
[statiske filer](https://parenteser.mattilsynet.io/lange-flate-filer/) på Google
Cloud Run. Miljøet er konfigurert av Terraform.

### Bygge bloggen for produksjon

```sh
make docker/build
```

Deretter kan du sjekke at ting virkelig er produksjonsklart:

```sh
http-server docker/build
```

### Oppsett av produksjonsmiljøet

Du må ha noen verktøy:

```sh
brew install terraform gh
```

For å sette opp miljøet må du ha en GCP-konto og tilgang til relevante
prosjekter.

Du må være autentisert mot GCP. Deretter setter du parenteser-prosjektet som
default og autentiserer maskinen din mot dette prosjektet:

```sh
glcoud auth login
gcloud config set project parenteser-b480
gcloud auth application-default login
```

Terraform henter noen moduler over https-git som ikke er åpne. For at det skal
funke kan du bruke github sin CLI for å autentisere deg for https:

```sh
gh auth login
```

Velg HTTPS og fullfører flyten som følger.

NÅ! Nå, er du klar for å kjøre opp ting:

```sh
cd tf
terraform init
terraform plan
terraform apply
```

Dette vil sette opp nødvendig infrastruktur. Merk at [terraform-oppsettet
vårt](./tf/main.tf) har et "hello world" image. Dette imaget brukes kun ved
første gangs oppsett. [Github
Actions-arbeidsflyten](.github/workflows/build.yml) ber CloudRun om å kjøre nye
images ved push.
