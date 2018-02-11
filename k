#!/bin/bash

# flags: --tree to get containment tree, --error to get syntax/type errors, --solve to get syntax/type errors and solve

#set -x

if [ -n "$M2_REPO" ]; then
    m2=$M2_REPO
else
    m2=${HOME}/.m2/repository
fi

solve=true

cd ${HOME}/git/kservices

pkg=generatedCode
for arg in "$@"; do
	if [ "${pkg}" = "true" ]; then
	  pkg=$arg
	fi
	case "$arg" in
    	"--error") solve=false;;
    	"--tree") solve=false;;
	"--package") pkg="true"
    esac
done
   

kToJavaOutput=$( { mvn -q -Pmbee-dev -DskipTests exec:java@generate -Dexec.args="--tree --error $*" ; } 2>&1 ) || true


solveOutput=
if [ "$solve" = true ] ; then 

    solveOutput=$( { mvn -q -DskipTests -Pmbee-dev compile exec:java@solve -DkPackageName=${pkg} ; } 2>&1 ) || true


    rm -f ${HOME}/git/kservices/target/classes/${pkg}/* || true

	#rm -f ${HOME}/git/kservices/src/${pkg}/* || true

	rm -f ${HOME}/git/kservices/src/gov/nasa/jpl/kservices/*.class || true
fi

#set -x
#echo "kToJavaOutput = $kToJavaOutput"
#echo "solveOutput = $solveOutput"

jsonOut=$solveOutput
if [ -z "$kToJavaOutput" ] || [ "$kToJavaOutput" = "?" ]; then
    kToJavaOutput='{}'
else
    if [ -z "$solveOutput" ]; then
      solveOutput='{}'
      jsonOut=$kToJavaOutput
    else
      jsonOut="{  ${kToJavaOutput:1: ${#kToJavaOutput}-2},  ${solveOutput:1: ${#solveOutput}-2}}"
    fi
fi

echo '**************'
printf "%s\n" "$jsonOut"

