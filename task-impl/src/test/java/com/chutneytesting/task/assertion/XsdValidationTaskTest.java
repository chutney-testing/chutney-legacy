package com.chutneytesting.task.assertion;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import com.chutneytesting.task.spi.TaskExecutionResult;
import com.chutneytesting.task.spi.TaskExecutionResult.Status;
import com.chutneytesting.task.spi.injectable.Logger;
import com.chutneytesting.task.TestLogger;

public class XsdValidationTaskTest {

	XsdValidationTask task;
	
	@Test
	public void should_validate_simple_xsd() {
		Logger logger = new TestLogger();
		String xml ="<?xml version=\"1.0\"?>\r\n" + 
				"<Employee xmlns=\"https://www.chutneytesting.com/Employee\">\r\n" + 
				"	<name>Pankaj</name>\r\n" + 
				"	<age>29</age>\r\n" + 
				"	<role>Java Developer</role>\r\n" + 
				"	<gender>Male</gender>\r\n" + 
				"</Employee>";
		
		String xsd = "/xsd_samples/employee.xsd";
		
		task = new XsdValidationTask(logger, xml, xsd);
		
		//When
		TaskExecutionResult result = task.execute();
		
		//Then
		assertThat(result.status).isEqualTo(Status.Success);
	}
	
	
	@Test
	public void should_not_validate_simple_xsd() {
		Logger logger = new TestLogger();
		String xml ="<?xml version=\"1.0\"?>\r\n" + 
				"<Employee xmlns=\"https://www.chutneytesting.com/Employee\">\r\n" + 
				"	<name>Pankaj</name>\r\n" + 
				"	<age>29</age>\r\n" + 
				"	<role>Java Developer</role>\r\n" + 
				"	<gender>ERROR</gender>\r\n" + 
				"</Employee>";
		
		String xsd = "/xsd_samples/employee.xsd";
		
		task = new XsdValidationTask(logger, xml, xsd);
		
		//When
		TaskExecutionResult result = task.execute();
		
		//Then
		assertThat(result.status).isEqualTo(Status.Failure);
	}

	
}
