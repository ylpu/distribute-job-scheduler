package com.ylpu.thales.scheduler.service.impl;

import java.util.ArrayList;
import java.util.List;
import javax.transaction.Transactional;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.ylpu.thales.scheduler.common.dao.BaseDao;
import com.ylpu.thales.scheduler.common.service.impl.BaseServiceImpl;
import com.ylpu.thales.scheduler.dao.SchedulerConnectionMapper;
import com.ylpu.thales.scheduler.entity.SchedulerConnection;
import com.ylpu.thales.scheduler.request.ConnectionRequest;
import com.ylpu.thales.scheduler.response.ConnectionResponse;
import com.ylpu.thales.scheduler.service.ConnectionService;
import com.ylpu.thales.scheduler.service.exception.ThalesRuntimeException;

@Service
@Transactional
public class ConnectionServiceImpl extends BaseServiceImpl<SchedulerConnection,Integer> implements ConnectionService {

    @Autowired
    private SchedulerConnectionMapper schedulerConnectionMapper;

    @Override
    protected BaseDao<SchedulerConnection, Integer> getDao() {
        return schedulerConnectionMapper;
    }

	@Override
	public PageInfo<ConnectionResponse> findAll(String connectionId,int pageNo,int pageSize) {
		PageHelper.startPage(pageNo,pageSize);

		List<SchedulerConnection> connectionList = schedulerConnectionMapper.findAll(connectionId);
		ConnectionResponse connectionResponse = null;
		Page<ConnectionResponse> page = new Page<ConnectionResponse>();
		if(connectionList != null && connectionList.size() > 0) {
			for(SchedulerConnection schedulerConnection : connectionList) {
				connectionResponse = new ConnectionResponse();
				BeanUtils.copyProperties(schedulerConnection, connectionResponse);
				page.add(connectionResponse);
			}
		}
		page.setTotal(schedulerConnectionMapper.getConnectionCount(connectionId));
		PageInfo<ConnectionResponse> pageInfo = new PageInfo<ConnectionResponse>(page);
        return pageInfo;
	}
	
	@Override
    public ConnectionResponse getConnection(String connectionId) {
		List<ConnectionResponse> responseList = new ArrayList<ConnectionResponse>();
		List<SchedulerConnection> connectionList = schedulerConnectionMapper.findAll(connectionId);
		if(connectionList == null || connectionList.size() == 0) {
			throw new ThalesRuntimeException("连接不存在");
		}else {
			ConnectionResponse connectionResponse = null;
			if(connectionList != null && connectionList.size() > 0) {
		      for(SchedulerConnection schedulerConnection : connectionList) {
			      connectionResponse = new ConnectionResponse();
				  BeanUtils.copyProperties(schedulerConnection, connectionResponse);
				  responseList.add(connectionResponse);
				}
			}
		}
		return responseList.get(0);
    }
	
	@Override
    public void addConnection(ConnectionRequest request) {
		List<SchedulerConnection> list = schedulerConnectionMapper.findAll(request.getConnectionId());
		if(list != null && list.size() > 0) {
			throw new ThalesRuntimeException("连接已经存在");
		}else {
	       if(request != null) {
        	       SchedulerConnection connection = new SchedulerConnection();
               BeanUtils.copyProperties(request, connection);
               insertSelective(connection);
           }
		}
    }
	
    @Override
    public void updateConnection(ConnectionRequest request) {  
       if(request != null) {
    	    SchedulerConnection connection = new SchedulerConnection();
        BeanUtils.copyProperties(request, connection);
        updateByPrimaryKeySelective(connection);
      }
    }	
}
