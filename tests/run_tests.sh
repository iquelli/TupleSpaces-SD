#!/bin/bash

ROOT_PATH=${PWD}/../
CLIENT_PATH=${PWD}/../Client
SERVER_PATH=${PWD}/../ServerR1
NAME_SERVER_PATH=${PWD}/../NameServer
TESTS_FOLDER=${PWD}
TESTS_OUT_EXPECTED=${TESTS_FOLDER}/expected
TESTS_OUTPUT=${TESTS_FOLDER}/test-outputs
N_TESTS=5

GREEN='\033[0;32m'
RED='\033[0;31m'
NC='\033[0m'

DIFF="diff"
if command -v colordiff &>/dev/null; then
    DIFF="colordiff"
fi

rm -rf $TESTS_OUTPUT
mkdir -p $TESTS_OUTPUT

cd $ROOT_PATH
mvn clean install -q

cd $NAME_SERVER_PATH
python server.py > /dev/null &

cd $SERVER_PATH
mvn exec:java -q -Dexec.args="2001 A" > /dev/null &

cd $CLIENT_PATH
for i in {1..5}; do
    echo "-------------------------------------------------------------------------------"
    TEST=$(printf "%02d" $i)
    time mvn compile exec:java -q < ${TESTS_FOLDER}/input$TEST.txt > ${TESTS_OUTPUT}/out$TEST.txt

    OUTPUT=$(${DIFF} ${TESTS_OUTPUT}/out$TEST.txt ${TESTS_OUT_EXPECTED}/out$TEST.txt)
    if [ "$OUTPUT" != "" ]; then
        echo "${RED}TEST [$TEST] FAILED${NC}"
        echo "###############################################################################"
        echo "${OUTPUT}"
    else
        echo "${GREEN}TEST [$TEST] PASSED${NC}"
    fi
done

killall java
killall python
