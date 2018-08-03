#!/bin/bash
CURRENT_TAG=`git describe --tags --exact-match $TRAVIS_COMMIT 2> /dev/null`
if [ "$TRAVIS_BRANCH" = "$CURRENT_TAG" ] && [ "$INTEGRATION_TESTS" != "true" ]
then
    mvn clean compile org.apache.felix:maven-bundle-plugin:bundle deploy -P release --settings settings.xml
fi
