package com.ylpu.kepler.scheduler.common.service;

import java.io.Serializable;

import com.ylpu.kepler.scheduler.entity.BaseEntity;

public interface BaseService<T extends BaseEntity,D extends Serializable> {
	
	public T findOneById(D Id);   

    public void deleteByPrimaryKey(D id);

    public int insertSelective(T record);

    public void updateByPrimaryKeySelective(T record);
}
