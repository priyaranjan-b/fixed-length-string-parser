package com.parser.model;

import com.parser.annotations.MQField;


/**
 * 
 * @author prbehera <br>
 * $Id$
 * @version 1.$Revision$ 
 */
public class EmployeeAddress {
	
	@MQField(position=1, length=4)
	private String flat_no;
	
	@MQField(position=2, length=6)
	private int pin;

	/**
	 * @return the flat_no
	 */
	public String getFlat_no() {
		return flat_no;
	}

	/**
	 * @param flat_no the flat_no to set
	 */
	public void setFlat_no(String flat_no) {
		this.flat_no=flat_no;
	}

	/**
	 * @return the pin
	 */
	public int getPin() {
		return pin;
	}

	/**
	 * @param pin the pin to set
	 */
	public void setPin(int pin) {
		this.pin=pin;
	}

}
