package com.parser.model;

import java.util.Date;

import com.parser.annotations.MQField;

/**
 * @author prbehera <br>
 *         $Id$
 * @version 1.$Revision$
 */
public class Employee {

	@MQField(position=1, length=6)
	public int emp_id;

	@MQField(position=2, length=20)
	private String name;

	@MQField(position=3, length=8)
	private String project_code;

//	@MQField(position=4, isComposite=true)
	private EmployeeAddress emp_address;

//	@MQField(position=5, length=3)
	private int length;

//	@MQField(position=6, length=3)
	private Integer[] commissions;

//	@MQField(position=7, length=3)
	private int nos_of_projects;

//	@MQField(position=8, isComposite=true)
	private EmployeeProjects[] projects;

	//@MQField(position=9, length=8)
	private Date joining_date;

	/**
	 * @return the joining_date
	 */
	public Date getJoining_date() {
		return joining_date;
	}

	/**
	 * @param joining_date the joining_date to set
	 */
	public void setJoining_date(Date joining_date) {
		this.joining_date=joining_date;
	}

	/**
	 * @return the nos_of_projects
	 */
	public int getNos_of_projects() {
		return nos_of_projects;
	}

	/**
	 * @param nos_of_projects the nos_of_projects to set
	 */
	public void setNos_of_projects(int nos_of_projects) {
		this.nos_of_projects=nos_of_projects;
	}

	/**
	 * @return the projects
	 */
	public EmployeeProjects[] getProjects() {
		return projects;
	}

	/**
	 * @param projects the projects to set
	 */
	public void setProjects(EmployeeProjects[] projects) {
		this.projects=projects;
	}

	/**
	 * @return the length
	 */
	public int getLength() {
		return length;
	}

	/**
	 * @param length the length to set
	 */
	public void setLength(int length) {
		this.length=length;
	}

	/**
	 * @return the commissions
	 */
	public Integer[] getCommissions() {
		return commissions;
	}

	/**
	 * @param commissions the commissions to set
	 */
	public void setCommissions(Integer[] commissions) {
		this.commissions=commissions;
	}

	/**
	 * @return the emp_id
	 */
	public int getEmp_id() {
		return emp_id;
	}

	/**
	 * @param emp_id the emp_id to set
	 */
	public void setEmp_id(int emp_id) {
		this.emp_id=emp_id;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name=name;
	}

	/**
	 * @return the project_code
	 */
	public String getProject_code() {
		return project_code;
	}

	/**
	 * @return the emp_address
	 */
	public EmployeeAddress getEmp_address() {
		return emp_address;
	}

	/**
	 * @param emp_address the emp_address to set
	 */
	public void setEmp_address(EmployeeAddress emp_address) {
		this.emp_address=emp_address;
	}

	/**
	 * @param project_code the project_code to set
	 */
	public void setProject_code(String project_code) {
		this.project_code=project_code;
	}

	@Override
	public String toString() {

		StringBuilder string=new StringBuilder();
		string.append( "Emp ID : "+emp_id+", Emp Name : "+name+", Project Code : "+project_code+" Emp Address :"
				+emp_address.getFlat_no()+", "+emp_address.getPin());
		string.append(", Commissions are : [");
		for (Integer commission : commissions) {
			string.append(commission+", ");
		}
		string.append("]");
		string.append(", Employee Projects : [");
		for (EmployeeProjects emp_project : projects) {
			string.append(emp_project.getProject_id()+", ");
		}
		string.append("]");
		string.append("Joining date :"+joining_date);
		return string.toString();
	}
}
