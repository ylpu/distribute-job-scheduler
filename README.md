# Introduction
kepler是一款自主研发的调度系统，系统支持调度sql,shell,spark等各种任务类型。

# Architecture
![image](https://git.qutoutiao.net/dataplatform/stream/kepler/blob/dev/docs/kepler-arch.png)

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
	    - resource manager --任务资源管理
	    - task schedule & submit --任务提交调度
	- worker --任务执行
	    - oss --任务日志
	    - execute thread -- 任务执行
	- core --核心实现
	- common --通用帮助类
- webapp --整个web系统
	- ui --web系统前端
	- backend --web系统后端
		- controller --后端系统控制层
		- dao --后端系统数据层
		- service --后端系统服务层
## 2. 启动
1.在kepler下面执行 mvn clean install -Dmaven.test.skip=true  
2.执行scheduler-controller/src/script/start.sh启动controller  
3.执行scheduler-master/src/script/start.sh启动master  
4.执行scheduler-worker/src/script/start.sh启动worker