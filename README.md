# Introduction
thales是一款自主研发的分布式任务调度系统，系统支持shell,hive,spark,python,http等各种类型任务的调用。

# Architecture
![image](https://github.com/ylpu/distribute-job-scheduler/tree/master/docs/thales-arch.png)

# Feature
* 分布式部署
* 基于真实资源
* 资源隔离，相同类型的任务可以提交到对应的资源池
* 支持大规模任务调度
* 节点掉线自动发现
* 任务失败后自动重试
* 任务失败后自动告警
* 可以通过页面调度，停止，查看任务详情和依赖等等
* 可以调度大数据任务
* 提供各种图表方便用户查看任务执行情况

# 目录结构

## 1. Project structure 

- scheduler
	- alert --任务告警
	- api --对外接口
		- controller --任务控制层
		- dao --任务数据层
		- service --任务服务层
		- common --通用帮助类
		--model --对外接口模型
	- master --任务调度
	    - quartz --任务扫描调度
	    - master manager --任务资源管理
	    - task schedule & submit --任务提交调度
	- worker --任务执行
	    - log --任务日志
	    - execute thread -- 任务执行
	- core --核心实现
# scheduler-master
master作为调度的核心，主要有如下功能：
* 初始化任务实例状态
* 通过quartz调度所有任务
* 检查任务状态
* 启动master rpc服务供worker汇报资源和心跳
* master ha
* 启动jetty server接受api的调用
* 根据机器cpu,内存，任务负载个数等选择最优的worker调度
* 提供节点的自动上线与下线

# scheduler-worker
worker作为执行器，主要有如下功能：
* 负责任务的执行，目前可执行shell,hive,spark任务
* 启动jetty server供查看任务日志，
* 上报资源信息，发送心跳给master.

# scheduler-api
api作为接口层，主要有如下功能：
* 和前端交互，负责任务调度，下线，重跑，看日志等。

# 系统运行
* 本地安装mysql,zookeeper,如非本地安装，需要修改controller,master,worker里config.properties,application.yml中的zookeeper和mysql地址和端口
* 在mysql数据库中执行distribute-job-scheduler/thales-scheduler/sql/thales-scheduler.sql
* git clone https://github.com/ylpu/distribute-job-scheduler.git ,将项目导入到intellij或eclipse中并安装lombok.
* 在distribute-job-scheduler中执行mvn clean install -Dmaven.test.skip=true
* 依次执行如下脚本
  * scheduler-controller/src/script/start-api.sh start ,启动成功后可以通过http://localhost:8085/swagger-ui.html 查看接口文档
  * scheduler-master/src/script/start-master.sh start,启动成功后可以通过 http://localhost:9095/ 查看master jmx信息
  * scheduler-worker/src/script/start-worker.sh start，启动后可以通过查看10001端口判断logserver是否成功
* 在mysql数据库中执行语句
  * insert into t_thales_scheduler_user(user_name,password,create_time,update_time) values ('test','test',CURRENT_TIMESTAMP,CURRENT_TIMESTAMP);
  
* 运行distribute-job-scheduler-frontend项目并以如上用户名，密码登陆
# 系统使用
* 创建任务（创建任务后点击调度）
  * 任务名称 ： 任务名称，必须唯一
  * 任务工作组： 任务会被提交到相应的组里
  * 任务依赖： 选择需要依赖的任务
  * 任务优先级： 任务的优先度，有低，中，高三种
  * 任务责任人：任务的所属人
  * 任务类型：目前command,shell,hive,spark四种
  * 任务告警人： 任务失败，超时的时候需要通知的人（邮箱格式）
  * 告警类型：有sms,webchat,email三种，目前只支持emai
  * 调度时间：cron表达式
  * 任务周期：分钟，小时，天，周，月，年等
  * 重试次数：任务失败时最多重试的次数
  * 超时时间：超过多少分钟任务将会被杀死。
  * 任务描述：任务的详细信息
  * 任务配置：根据类型不同有不同的配置（以下为简单例子）：
    * command: {"commandLine" :"pwd;cat /tmp/log/scheduler-worker/info.log"}
    * shell: {"fileName" : "/tmp/shell/test.sh","parameters" : {"param1":"test"}}
    * python: {"fileName" : "/tmp/python/test.py","parameters" : {"param1":"test"}}
    * hive: {"fileName" : "/tmp/shell/test.sh","parameters" : "","placeHolder":{"dt":"20191205","hm":"1000"}
    * spark: {"fileName":"/tmp/shell/test.sql","parameters":{"masterUrl":"spark://localhost:7077","executorMemory":"2g","executorCores":2,"totalExecutorCores":20},"placeHolder":{"dt":"20191205","hm":"1000"}
    * http:
      * get:{"url":"http://localhost:8085/api/job/getJobById",
"method": "get",
"parameters":{
  "id":45
}
}
      * post:{"url":"http://localhost:8085/api/job/addJob",
"method": "post",
"parameters":{
  "alertTypes": "email",
  "alertUsers": "string",
  "creatorId": "string",
  "dependIds": [],
  "description": "string",
  "executionTimeout": 0,
  "isSelfdependent": true,
  "jobConfiguration": "string",
  "jobCycle": "DAY",
  "jobName": "rest-test",
  "jobPriority": "HIGH",
  "jobReleasestate": 0,
  "jobType": "shell",
  "maxRetrytimes": 0,
  "ownerIds": "test",
  "retryInterval": 0,
  "scheduleCron": "0 3 15 * * ?",
  "workerGroupname": "hive"
}
}
     * sql : {
 "datasource":{
  "dbType":"mysql",
  "url": "jdbc:mysql://localhost:3306/thales?characterEncoding=utf8",
  "userName":"root",
  "password":123456
  },
  "operator":"select",
  "sql":"select * from t_thales_scheduler_job_instance where id = ?",
  "parameters":{"param1":76}
}
 * 修改任务
   * 参数与创建任务相同（修改后需要点击重新调度）
 * 任务图
   * 查看整个任务的依赖关系
 * 实例图
   * 图表方式查看任务近30天的运行情况

  "url": "jdbc:mysql://localhost:3306/thales?characterEncoding=utf8",
