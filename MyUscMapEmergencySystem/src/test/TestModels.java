package test;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

import java.awt.Point;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import main.QueryDrawGeoEngine;
import models.AnnouncementSystem;
import models.Building;
import models.Student;

import org.junit.BeforeClass;
import org.junit.Test;
/**
 * simple test case using mockito and junit for QueryDrawGeoEngine Class, method queryWhole
 * @author sidawang
 */
public class TestModels {
	static QueryDrawGeoEngine qdgeMockito;
	static Map<String, Boolean> featureMap;
	//static Student stu;
	@BeforeClass
	public static void setUp() {
		ArrayList<String> log ;
		log = new ArrayList<String>();
		log.add("test");

	//	stu = new Student("stu1", new Point(100, 100));
		
		qdgeMockito = mock(QueryDrawGeoEngine.class);
		
		featureMap = new HashMap<String, Boolean>();
		featureMap.put("students", true);
		
		when(qdgeMockito.queryWhole(featureMap)).thenReturn(log);
	}

	@Test
	public void testQueryWhole(){
		//mockito query
		ArrayList<String> mockLog = qdgeMockito.queryWhole(featureMap);
		System.out.println(mockLog);
		//Junit test
		assertEquals(mockLog.get(0), "test");
		
		//verify queryWhole executed
		verify(qdgeMockito).queryWhole(featureMap);
	}
	/*@Test
	public void queryStudents(){
		
	}
	*/
}
