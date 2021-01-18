package com.ylpu.thales.scheduler.request;

import java.io.Serializable;
import lombok.Data;

@Data
public class ConnectionRequest implements Serializable {

    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private Integer id;

    private String connectionId;

    private String connectionType;

    private String hostname;

    private String dbSchema;

    private Integer port;

    private String username;

    private String password;
}