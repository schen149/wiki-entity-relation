#!/bin/sh
if [ "$#" -ne 2 ]; then 
	echo "\nUsage: ./demo.sh [input-file-list] [top-k-entity]\n"
	echo "The first argument should be the path to input file. Each line of the input file should contain a wikipedia page link/title, such as \"Champaign,_Illinois\"."
	echo "The second argument is a number k, indicating the number of entities you want to retrieve." 
	echo "An example input file is included located at sample-input/ under the project folder"
exit
fi

mvn exec:java -Dexec.arguments="-Xmx1g" -Dexec.mainClass=edu.illinois.cs.cogcomp.wikirelation.demo.DemoPageIDLinker -Dexec.args="/shared/experiments/schen149/wiki-entity-relation/config/cogcomp-english-170601.properties $1 $2"
