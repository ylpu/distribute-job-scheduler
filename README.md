# Introduction
thales是一款自主研发的分布式任务调度系统，系统支持shell,hive,spark等各种类型任务的调用。

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
* 安装mysql,zookeeper
* 在mysql数据库中执行distribute-job-scheduler/thales-scheduler/sql/thales-scheduler.sql
* 在distribute-job-scheduler中执行mvn clean install -Dmaven.test.skip=true
* 执行如下脚本
  * scheduler-controller/src/script/start-api.sh
  * scheduler-master/src/script/start-master.sh
  * scheduler-worker/src/script/start-worker.sh
