#!/bin/bash
script_folder=$(cd `dirname $0`;pwd)
parent_dir="$(dirname "$script_folder")"
base_dir="$(dirname "$parent_dir")"

if [ "$#" -ne 1 ]
  then
    echo "/bin/bash $0 {start|stop|restart}"
  exit 1
fi

function start(){
    nohup java -Dconfig.file=$parent_dir/config/config.properties -Dlog4j.configurationFile=$parent_dir/config/log4j2.xml -jar $parent_dir/target/scheduler-worker-1.0.0.jar &
    code=$?

  if [ $code -eq 0 ]
     then
      echo "worker启动成功!"
     else
      echo "worker启动失败!"
      exit $code
    fi
}

function stop(){
    pid=$(jps -m | grep scheduler-worker | awk '{print $1}')
    if [ ! $pid ]
     then
      echo "worker已经停止"
     else
       kill -15 $pid
       echo "worker成功停止"
    fi
}

case $1 in
  start) 
     start 
  ;; 
  stop) 
     stop 
  ;; 
  restart) 
     stop 
     start 
  ;; 
esac