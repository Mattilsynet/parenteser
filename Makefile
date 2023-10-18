VERSION = $$(git rev-parse --short=10 HEAD)
IMAGE = europe-north1-docker.pkg.dev/artifacts-352708/mat/parenteser:$(VERSION)

docker/build:
	clojure -X:dev:build

docker: docker/build
	cd docker && docker build -t $(IMAGE) .

publish:
	docker push $(IMAGE)

test:
	clojure -M:dev -m kaocha.runner

clean:
	rm -fr docker/build

.PHONY: clean docker publish test
