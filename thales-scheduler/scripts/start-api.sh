#!/bin/bash
script_folder=$(cd `dirname $0`;pwd)
parent_dir="$(dirname "$script_folder")"
base_dir="$(dirname "$parent_dir")"
java -jar $base_dir/target/scheduler-controller-0.0.1-SNAPSHOT.jar --server.port=8085
code=$?
exit $code