package com.ylpu.thales.scheduler.request;

import java.io.Serializable;
import lombok.Data;

@Data
public class RoleRequest implements Serializable {
	
    private Integer id;

    private String roleName;

}