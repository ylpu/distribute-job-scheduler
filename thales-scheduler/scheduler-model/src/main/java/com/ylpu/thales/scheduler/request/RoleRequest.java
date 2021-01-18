package com.ylpu.thales.scheduler.request;

import java.io.Serializable;
import lombok.Data;

@Data
public class RoleRequest implements Serializable {

    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private Integer id;

    private String roleName;

}