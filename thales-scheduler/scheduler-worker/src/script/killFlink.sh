#!/bin/bash

jobName=$1
yarnCommand=$2/bin/yarn

for i in `$yarnCommand application -list | grep $jobName | awk '${print $1}' | grep -i Application_`
do
  yarn application -kill $i
done