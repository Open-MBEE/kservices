#!/bin/bash
#set -x

if [ -z $pkg ]; then
  pkg=$1
fi
if [ -z $pkg ]; then
  pkg=k
fi

currentDir=`pwd`
export DATA_DIR=`ls -1trd ${currentDir}/output/* | tail -n 1`
export TMS_SCENARIO_NAME=${pkg}
../baeModels/europa/data/pushToTms.sh 2>&1 > ravenPush_${pkg}.log
echo "see RAVEN folder ${pkg} at https://europamps1.jpl.nasa.gov:9443/mpsserver/raven/"
