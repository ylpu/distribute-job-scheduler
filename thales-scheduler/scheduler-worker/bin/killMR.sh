#!/bin/bash

jobName=$1
hadoopCommand=$2/bin/hadooop

for i in `$hadoopCommand job -list | grep $jobName | awk '${print $1}' | grep job_`
do
  hadoop job -kill $i
done