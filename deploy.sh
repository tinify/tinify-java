#!/bin/bash
CURRENT_TAG=`git describe --tags --exact-match $TRAVIS_COMMIT 2> /dev/null`
# if [ "$TRAVIS_BRANCH" = "$CURRENT_TAG" ] && [ "$INTEGRATION_TESTS" != "true" ]
# then
mvn clean compile org.apache.felix:maven-bundle-plugin:bundle deploy -Dmaven.test.skip=true -P release -DdryRun=true --settings settings.xml
#fi
stat --printf="%s" "target/tinify-1.6.1.jar"
