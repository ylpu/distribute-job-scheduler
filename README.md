# Introduction
thales是一款自主研发的调度系统，系统支持调度shell,hive,spark等各种任务类型。

# Architecture
![image](https://github.com/ylpu/distribute-job-scheduler/tree/master/docs/thales-arch.png)

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
