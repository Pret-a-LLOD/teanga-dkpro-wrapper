#!/bin/bash

mvn clean install exec:java

for f in `ls dockers/`
do
    cd dockers/$f
    mvn install
    docker build . -t pretallod/teanga-dkpro-wrapper-$f
    cd -
done
