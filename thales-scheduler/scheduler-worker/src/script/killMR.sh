#!/bin/bash

paramCount=$#

if [[ paramCount < 1 ]];then
   echo "please input jobname"
   exit
fi

jobName=$1
hadoopCommand=$2/bin/hadooop

if [ ! -n "$1" ];then
  echo "jobName can not empty"
  exit
fi

if [ ! -n "$2" ];then
  hadoopCommand=hadoop
fi

for i in `$hadoopCommand job -list | grep $jobName | awk '${print $1}' | grep job_`
do
  hadoop job -kill $i
done