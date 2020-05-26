package com.ylpu.thales.scheduler.request;

import java.io.Serializable;
import java.util.List;
import lombok.Data;

@Data
public class UserRoleRequest implements Serializable {
    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    private Integer id;
    private List<Integer> roleIds;
}
