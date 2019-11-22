package com.ylpu.thales.scheduler.service.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.transaction.Transactional;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.ylpu.thales.scheduler.common.dao.BaseDao;
import com.ylpu.thales.scheduler.common.service.impl.BaseServiceImpl;
import com.ylpu.thales.scheduler.common.utils.DateUtils;
import com.ylpu.thales.scheduler.dao.SchedulerWorkerMapper;
import com.ylpu.thales.scheduler.entity.SchedulerWorker;
import com.ylpu.thales.scheduler.enums.NodeStatus;
import com.ylpu.thales.scheduler.request.WorkerGroupRequest;
import com.ylpu.thales.scheduler.request.WorkerRequest;
import com.ylpu.thales.scheduler.response.WorkerResponse;
import com.ylpu.thales.scheduler.service.WorkerService;

@Service
@Transactional
public class WorkerServiceImpl extends BaseServiceImpl<SchedulerWorker,Integer> implements WorkerService {

    @Autowired
    private SchedulerWorkerMapper schedulerWorkerMapper;

    @Override
    protected BaseDao<SchedulerWorker, Integer> getDao() {
        return schedulerWorkerMapper;
    }

    @Override
    public void addWorker(WorkerRequest request) {
        SchedulerWorker worker = new SchedulerWorker();
        if(request != null) {
            BeanUtils.copyProperties(request, worker);
            insertSelective(worker);   
        }
    }
    
    @Override
    public void updateWorkerByHost(WorkerRequest request) {
        SchedulerWorker worker = new SchedulerWorker();
        if(request != null) {
            BeanUtils.copyProperties(request, worker);
            worker.setUpdateTime(new Date());
            schedulerWorkerMapper.updateWorkerByHost(worker);  
        }
    }

    @Override
    public void updateWorkersStatusByGroup(WorkerGroupRequest param) {
        Map<String,Object> map = new HashMap<String,Object>();
        map.put("groupName", param.getGroupName());
        map.put("workers", param.getWorkers());
        map.put("status",param.getStatus().getCode());
        schedulerWorkerMapper.updateWorkersStatusByGroup(map);
    }

    @Override
    public List<WorkerResponse> getWorkersInfoByGroup(WorkerGroupRequest param) {
        List<WorkerResponse> list = new ArrayList<WorkerResponse>();
        Map<String,Object> map = new HashMap<String,Object>();
        map.put("groupName", param.getGroupName());
        map.put("workers", param.getWorkers());
        List<SchedulerWorker> workers = schedulerWorkerMapper.getWorkersInfoByGroup(map);
        WorkerResponse response = null;
        if(workers != null && workers.size() > 0) {
            for(SchedulerWorker worker : workers) {
                response = new WorkerResponse();
                BeanUtils.copyProperties(worker, response);
                list.add(response);
            }
        }
        return list;
    }

    @Override
    public void insertOrUpdateWorker(WorkerRequest request) {
        WorkerGroupRequest param = new WorkerGroupRequest();
        param.setGroupName(request.getNodeGroup());
        param.setWorkers(Arrays.asList(request.getHost()));
        List<WorkerResponse> list = getWorkersInfoByGroup(param);
        if(list == null || list.size() == 0) {
            addWorker(request);
        }else {
            updateWorkerByHost(request);
        }
    }

	@Override
	public PageInfo<WorkerResponse> findAll(String nodeGroup,String worker,int pageNo,int pageSize) {
		PageHelper.startPage(pageNo,pageSize);

		List<SchedulerWorker> workerList = schedulerWorkerMapper.findAll(nodeGroup, worker);
		WorkerResponse workerResponse = null;
		Page<WorkerResponse> page = new Page<WorkerResponse>();
		if(workerList != null && workerList.size() > 0) {
			for(SchedulerWorker schedulerWorker : workerList) {
				workerResponse = new WorkerResponse();
				BeanUtils.copyProperties(schedulerWorker, workerResponse);
				workerResponse.setNodeStatus(NodeStatus.getNodeStatus(schedulerWorker.getNodeStatus()));
				workerResponse.setLastHeartbeatTime(DateUtils.getDateAsString(schedulerWorker.getLastHeartbeatTime(),DateUtils.DATE_TIME_FORMAT));
				page.add(workerResponse);
			}
		}
		page.setTotal(schedulerWorkerMapper.getAllWorkers());
		PageInfo<WorkerResponse> pageInfo = new PageInfo<WorkerResponse>(page);
        return pageInfo;
	}

	@Override
	public List<String> getAvailiableWorkerGroup() {
		return schedulerWorkerMapper.getAvailiableWorkerGroup();
	}
}
