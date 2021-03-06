source-to-image:
	find node_modules -name ".git" | rev | cut -c6- | rev | xargs rm -fr
	npm install
	npm run build
	export MAVEN_OPTS="-Xmx512m"
	mvn clean install -DskipTests
