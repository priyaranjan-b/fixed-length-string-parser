package com.parser.model;

import com.parser.annotations.MQField;


/**
 * 
 * @author prbehera <br>
 * $Id$
 * @version 1.$Revision$ 
 */
public class EmployeeProjects {

	@MQField(position=1, length=6)
	private int project_id;

	/**
	 * @return the project_id
	 */
	public int getProject_id() {
		return project_id;
	}

	/**
	 * @param project_id the project_id to set
	 */
	public void setProject_id(int project_id) {
		this.project_id=project_id;
	}

}
