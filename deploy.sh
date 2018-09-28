#!/bin/bash
CURRENT_TAG=`git describe --tags --exact-match $TRAVIS_COMMIT 2> /dev/null`

if [ "$TRAVIS_BRANCH" = "$CURRENT_TAG" ] && [ "$DEPLOY_ON_SUCCESS" = "true" ]
then
  mvn clean compile org.apache.felix:maven-bundle-plugin:bundle deploy -Dmaven.test.skip=true -P release --settings settings.xml
fi
