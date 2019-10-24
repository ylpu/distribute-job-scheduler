package com.ylpu.thales.scheduler.common.service.impl;

import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import org.I0Itec.zkclient.ZkClient;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.transaction.annotation.Transactional;

import com.ylpu.thales.scheduler.common.config.Configuration;
import com.ylpu.thales.scheduler.common.constants.GlobalConstants;
import com.ylpu.thales.scheduler.common.dao.BaseDao;
import com.ylpu.thales.scheduler.common.service.BaseService;
import com.ylpu.thales.scheduler.common.zk.ZKHelper;
import com.ylpu.thales.scheduler.entity.BaseEntity;

@Transactional
public abstract class BaseServiceImpl<T extends BaseEntity,D extends Serializable>
        implements BaseService<T,D> {
    
    private static Log LOG = LogFactory.getLog(BaseServiceImpl.class);


    protected abstract BaseDao<T,  D> getDao();

    protected Class<T> entityClazz;

    @SuppressWarnings("unchecked")
    public BaseServiceImpl() {
        Type genType = getClass().getGenericSuperclass();
        Type[] params = ((ParameterizedType) genType).getActualTypeArguments();
        entityClazz = (Class<T>) params[0];
    }

    @Transactional(readOnly = true)
    public T findOneById(D Id) {
        return getDao().selectByPrimaryKey(Id);
    }
    
    @Transactional(readOnly = true)
    public void deleteByPrimaryKey(D id) {
        getDao().deleteByPrimaryKey(id);
    }

    public int insertSelective(T record) {
        record.setCreateTime(new Date());
        record.setUpdateTime(new Date());
        return getDao().insertSelective(record);
    }

    public void updateByPrimaryKeySelective(T record) {
        record.setUpdateTime(new Date());
        getDao().updateByPrimaryKeySelective(record);
    }
    
    public void updateByPrimaryKey(T record) {
        record.setUpdateTime(new Date());
        getDao().updateByPrimaryKey(record);
    }
    
    public String getMasterServiceUri(int id) {
        
        Properties prop = Configuration.getConfig(GlobalConstants.CONFIG_FILE);
        String quorum = prop.getProperty("thales.zookeeper.quorum");
        int sessionTimeout = Configuration.getInt(prop, "thales.zookeeper.sessionTimeout", GlobalConstants.ZOOKEEPER_SESSION_TIMEOUT);
        int connectionTimeout = Configuration.getInt(prop, "thales.zookeeper.connectionTimeout", GlobalConstants.ZOOKEEPER_CONNECTION_TIMEOUT);
        int masterRetryInterval = Configuration.getInt(prop, "thales.master.retry.interval", 1000);

        ZkClient zkClient = null;
        List<String> masters = null;
        while(true) {
            try {
                zkClient = ZKHelper.getClient(quorum,sessionTimeout,connectionTimeout);
                masters = zkClient.getChildren(GlobalConstants.MASTER_GROUP);
                if(masters != null && masters.size() > 0) {
                    StringBuilder sb = new StringBuilder("http://");
                    sb.append(masters.get(0).split(":")[0]);
                    sb.append(":");
                    sb.append(Configuration.getInt(prop, "thales.master.service.port", 9090));
                    if(isMasterAlive(sb.toString())){
                        return sb.append("/api/").toString();
                    }else {
                        try {
                            Thread.sleep(masterRetryInterval);
                        } catch (InterruptedException e) {
                            LOG.error(e);
                        }
                    }
                }else {
                    try {
                        Thread.sleep(masterRetryInterval);
                    } catch (InterruptedException e) {
                        LOG.error(e);
                    }
                }
            }catch(Exception e) {
                LOG.error(e);
            }finally {
                if(zkClient != null) {
                    zkClient.close();
                }
            }  
        }
    }
    
    public boolean isMasterAlive(String url) {
        boolean isAlive=true;
        HttpURLConnection conn = null;
        try {
            URL theURL = new URL(url);
            conn = (HttpURLConnection)theURL.openConnection();
            conn.setConnectTimeout(20000);
            conn.connect();
            int code = conn.getResponseCode();
            boolean success = (code >= 200) && (code < 300);
            if(!success){
                isAlive=false;
            }
        }catch(MalformedURLException e){
            isAlive=false;
        }catch(IOException e){
            isAlive=false;
        }catch(Exception e){
            isAlive=false;
        }
        return isAlive;
    }
}