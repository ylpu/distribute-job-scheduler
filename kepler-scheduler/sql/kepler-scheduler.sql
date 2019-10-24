CREATE TABLE `t_kepler_scheduler_job` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `job_name` varchar(512) DEFAULT NULL COMMENT '任务名称',
  `job_type` tinyint(4) DEFAULT NULL COMMENT '任务类型，1-sql,2-spark,3-shell',
  `job_configuration` longtext DEFAULT NULL COMMENT '任务配置',
  `job_priority` int(4) DEFAULT NULL COMMENT '任务优先级,1-low,2,middle,3-high',
  `creator_id` varchar(50) COMMENT '任务创建人',
  `owner_ids` varchar(512) DEFAULT NULL COMMENT '任务owner',
  `alert_users` varchar(512) DEFAULT NULL COMMENT '任务告警人',
  `alert_types` tinyint(4) DEFAULT NULL COMMENT '任务告警类型 1-sms,2-wechat,3-email',
  `schedule_cron` varchar(50) DEFAULT NULL COMMENT '任务执行表达式',
  `is_selfDependent` tinyint(1)  DEFAULT NULL COMMENT '是否自依赖',
  `job_cycle` tinyint(4) DEFAULT NULL COMMENT '任务执行周期 1-minute,2-hour,3-day,4-week,5-month,6-year',
  `max_retrytimes` int(4) DEFAULT NULL COMMENT '任务重试次数',
  `retry_interval` int(10) DEFAULT NULL COMMENT '重试间隔',
  `execution_timeout` int(10) DEFAULT '0' COMMENT '执行超时时间',
  `worker_groupName` varchar(50) DEFAULT NULL COMMENT '任务机器组',
  `job_ReleaseState` tinyint(4)  DEFAULT NULL COMMENT '任务状态，-1-删除，0-上线，1-下线',
  `description` varchar(512) DEFAULT NULL COMMENT '任务描述',
  `create_time` datetime DEFAULT NULL COMMENT '任务创建时间',
  `update_time` datetime DEFAULT NULL COMMENT '任务修改时间',
  PRIMARY KEY (`id`)
 ) ENGINE=InnoDB AUTO_INCREMENT=29 DEFAULT CHARSET=utf8;

CREATE TABLE `t_kepler_scheduler_job_instance` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `job_id` int(11) DEFAULT NULL COMMENT '任务id',
  `task_state` tinyint(4) DEFAULT NULL COMMENT '任务状态,1-SUBMIT, 2-PENDING, 3-WAITING, 4-RUNNING, 5-KILL, 6-SUCCESS, 7-FAIL',
  `log_url` varchar(512) DEFAULT NULL COMMENT '任务日志url',
  `log_path` varchar(512) DEFAULT NULL COMMENT '任务日志路径',
  `worker` varchar(50) DEFAULT NULL COMMENT '任务执行节点',
  `creator_name` varchar(50) DEFAULT NULL COMMENT '任务触发人',
  `creator_email` varchar(50) DEFAULT NULL COMMENT '任务触发人邮件',
  `retry_times` tinyint(4) DEFAULT NULL COMMENT '重试次数',
  `pid` int(11) DEFAULT NULL COMMENT '任务进程id',
  `applicationId` varchar(50) DEFAULT NULL COMMENT 'yarn任务应用id',
  `schedule_time` datetime DEFAULT NULL COMMENT '任务预计开始时间，比如任务预计10点执行，但由于quartz调度器非常繁忙，有可能10点01分才开始执行',
  `start_time` datetime DEFAULT NULL COMMENT '任务真实开始时间',
  `end_time` datetime DEFAULT NULL COMMENT '任务结束时间',
  `elapse_time` int(11) DEFAULT NULL COMMENT '任务花费时间',
  `create_time` datetime DEFAULT NULL COMMENT '任务创建时间',
  `update_time` datetime DEFAULT NULL COMMENT '任务修改时间',
  PRIMARY KEY (`id`)
 ) ENGINE=InnoDB AUTO_INCREMENT=29 DEFAULT CHARSET=utf8;

CREATE TABLE `t_kepler_scheduler_job_relation` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `job_id` int(11) DEFAULT NULL COMMENT '任务id',
  `parentJob_id` int(11) DEFAULT NULL COMMENT '父任务id',
  `create_time` datetime DEFAULT NULL COMMENT '任务创建时间',
  `update_time` datetime DEFAULT NULL COMMENT '任务修改时间',
  PRIMARY KEY (`id`)
 ) ENGINE=InnoDB AUTO_INCREMENT=29 DEFAULT CHARSET=utf8;

CREATE TABLE `t_kepler_scheduler_node` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `node_type` tinyint(4) DEFAULT NULL COMMENT '节点类型，1-master,2-worker',
  `host` varchar(128) DEFAULT NULL COMMENT '节点主机名称',
  `port` int(4) DEFAULT NULL COMMENT '节点端口',
  `node_group` varchar(50) DEFAULT NULL COMMENT '节点组',
  `zkDirectory` varchar(512) DEFAULT NULL COMMENT '节点目录',
  `cpu_usage` double(5,2) DEFAULT NULL COMMENT 'cpu使用率',
  `memory_usage` double(5,2) DEFAULT NULL COMMENT '内存使用率',
  `last_heartbeat_time` datetime DEFAULT NULL COMMENT '节点心跳时间',
  `node_status` tinyint(4) DEFAULT NULL COMMENT '节点状态，1-added,2-removed',
  `create_time` datetime DEFAULT NULL COMMENT '节点创建时间',
  `update_time` datetime DEFAULT NULL COMMENT '节点修改时间',
  PRIMARY KEY (`id`)
 ) ENGINE=InnoDB AUTO_INCREMENT=29 DEFAULT CHARSET=utf8;