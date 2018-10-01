#!/bin/bash
CURRENT_TAG=`git describe --tags --exact-match $TRAVIS_COMMIT 2> /dev/null`

# [ "$TRAVIS_BRANCH" = "$CURRENT_TAG" ] &&
if [ "$DEPLOY_ON_SUCCESS" = "true" ]
then
  echo "$GPG_PASSPHRASE"
  mvn clean compile org.apache.felix:maven-bundle-plugin:bundle deploy -Dmaven.test.skip=true -P release --settings settings.xml
fi
