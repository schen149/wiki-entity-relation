#!/bin/sh
mvn install
mvn exec:java -Dexec.arguments="-Xmx512m" -Dexec.mainClass=edu.illinois.cs.cogcomp.wikirelation.demo.DemoPageIDLinker -Dexec.args="/shared/experiments/schen149/wiki-entity-relation/config/cogcomp-english-170601.properties"