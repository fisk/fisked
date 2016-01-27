#!/usr/bin/env bash

mvn install:install-file -Dfile=abcl.jar -DgroupId=org.abcl -DartifactId=org.abcl.core -Dversion=1.3.3 -Dpackaging=jar

