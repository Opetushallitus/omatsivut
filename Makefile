source-to-image:
	find node_modules -name ".git" | rev | cut -c6- | rev | xargs rm -fr
	(cd hakemuseditori && npm install && ./gulp)
	npm install
	./gulp
	export MAVEN_OPTS="-Xmx512m"
	mvn clean install -DskipTests