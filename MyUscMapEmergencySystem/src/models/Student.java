package models;

import java.awt.Point;

/**
 * @author sidawang
 * Student model: ID and point(position)
 */
public class Student {
	String studentID;
	private Point point;

	public Student(String studentID, Point point) {
		super();
		this.studentID = studentID;
		this.setPoint(point);
	}

	public Point getPoint() {
		return point;
	}

	public void setPoint(Point point) {
		this.point = point;
	}
	@Override
	public String toString() {
		String s= studentID;
		s+= "\n" + point;
		return s;
	}
}
