#!/bin/sh
###################
. /etc/profile
. ~/.bash_profile
##################
echo =====================================`date`===============================================
SCRIPT_NAME=$(readlink -f "$0")
dir=`dirname ${SCRIPT_NAME}`
cd $dir

function get_child_pids ()
{
    c_pid=$1
    while [ ! -z ${c_pid} ]; do
        c_pids=${c_pids}" "${c_pid}
        c_pid=`ps -ef|awk '{print $2" "$3}'|grep " ${c_pid}"|awk '{print $1}'`
        [ -z "${c_pid}" ] && return
        for i in ${c_pid}; do
            get_child_pids $i
        done
    done
}

if [[ -z $1 ]]
   then
      pid=`cat $dir/conf/pid1 | awk -F ':' '{print $2}'`
   else
      pid=$1
fi
get_child_pids "$pid"
echo $c_pids
for item in ${c_pids};do
   kill -9 ${item}
done