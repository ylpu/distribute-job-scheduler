package com.ylpu.thales.scheduler.response;

import java.io.Serializable;
import lombok.Data;

@Data
public class RoleResponse implements Serializable {
	
    private Integer id;

    private String roleName;

}