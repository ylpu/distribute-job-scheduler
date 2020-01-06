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
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.curator.framework.CuratorFramework;
import org.springframework.transaction.annotation.Transactional;
import com.ylpu.thales.scheduler.common.config.Configuration;
import com.ylpu.thales.scheduler.common.constants.GlobalConstants;
import com.ylpu.thales.scheduler.common.curator.CuratorHelper;
import com.ylpu.thales.scheduler.common.dao.BaseDao;
import com.ylpu.thales.scheduler.common.service.BaseService;
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
    
    @Transactional(readOnly = false)
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
}