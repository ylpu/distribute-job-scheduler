package com.ylpu.thales.scheduler.response;

import java.io.Serializable;
import lombok.Data;

@Data
public class ConnectionResponse implements Serializable {

    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private Integer id;

    private String connectionId;

    private String connectionType;

    private String hostname;

    private Integer port;

    private String dbSchema;

    private String username;

    private String password;
}