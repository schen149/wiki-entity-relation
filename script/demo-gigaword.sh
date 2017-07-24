#!/usr/bin/env bash
if [ "$#" -ne 2 ]; then
	echo "\nUsage: ./demo-gigaword.sh [input-file-list] [top-k-entity]\n"
	echo "The first argument should be the path to input file. Each line of the input file should contain a wikipedia page link/title, such as \"Champaign,_Illinois\"."
	echo "The second argument is a number k, indicating the number of entities you want to retrieve."
	echo "An example input file is included located at sample-input/ under the project folder"
exit
fi
CONFIG="/shared/experiments/schen149/wiki-entity-relation/config/cogcomp-gigaword.properties"
LIBDIR="/shared/experiments/schen149/wiki-entity-relation/target/lib"
DISTDIR="/shared/experiments/schen149/wiki-entity-relation"

CP=.

for JAR in `ls $DISTDIR/*jar`; do
	CP="$CP:$JAR"
done

for JAR in `ls $LIBDIR/*jar`; do
	CP="$CP:$JAR"
done

CLASS="edu.illinois.cs.cogcomp.wikirelation.demo.Demo"
CMD="java -Xmx1g -cp $CP $CLASS $CONFIG $1 $2"

$CMD

exit