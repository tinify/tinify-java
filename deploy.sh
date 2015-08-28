#!/bin/bash
CURRENT_TAG=`git describe --tags --exact-match $TRAVIS_COMMIT 2> /dev/null`
if [ "$TRAVIS_BRANCH" = "$CURRENT_TAG" ]
then
    mvn clean deploy -P release --settings settings.xml
fi
