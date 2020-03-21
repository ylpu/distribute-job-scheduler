#!/bin/bash

paramCount=$#

if [[ paramCount < 1 ]];then
   echo "please input jobname"
   exit
fi

jobName=$1
yarnCommand=$2/bin/yarn

if [ ! -n "$1" ];then
  echo "jobName can not empty"
  exit
fi

if [ ! -n "$2" ];then
  yarnCommand=yarn
fi

for i in `$yarnCommand application -list -appTypes SPARK | grep $jobName | awk '${print $1}' | grep -i Application_`
do
  yarn application -kill $i
done