#!/bin/bash
script_folder=$(cd `dirname $0`;pwd)
parent_dir="$(dirname "$script_folder")"
base_dir="$(dirname "$parent_dir")"
java -jar $base_dir/target/scheduler-master-1.0.0-release.jar
code=$?
exit $code