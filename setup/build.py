import sys

jredis_server = """#!/bin/bash
PWD=$(pwd)
SCRIPT_PATH=$(readlink -f "$0")
cd $(dirname $SCRIPT_PATH)
java -jar ../JRedis-1.0-SNAPSHOT-jar-with-dependencies.jar $@ &
cd $PWD"""

jredis_client = """#!/bin/bash
PWD=$(pwd)
SCRIPT_PATH=$(readlink -f "$0")
cd $(dirname $SCRIPT_PATH)
python ../py/entry.py $@
cd $PWD"""
if sys.argv[1] == 'server':
    print(jredis_server)
else:
    print(jredis_client)
