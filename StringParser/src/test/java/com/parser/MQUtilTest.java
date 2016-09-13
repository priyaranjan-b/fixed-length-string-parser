package com.parser;

import org.junit.Test;

import com.parser.model.Employee;
import com.parser.util.ParserException;
import com.parser.util.StringParserUtil;

public class MQUtilTest {

	
	@Test
	public void testParse(){
		StringParserUtil mqUtil = new StringParserUtil(buildMessage(),false);
		try {
		Object obj =	mqUtil.getReferenceObject(Employee.class);
		} catch (ParserException e) {
			e.printStackTrace();
		}
	}
	
	private String buildMessage(){
		StringBuilder sb = new StringBuilder();
		sb.append("123456");
		sb.append("Priyaranjan B       ");
		sb.append("87654321");
		sb.append("123456");
		sb.append("123456");
		sb.append("123456");
		sb.append("123456");
		sb.append("123456");
		sb.append("123456");
		sb.append("123456");
		
		return sb.toString();
	}
}
