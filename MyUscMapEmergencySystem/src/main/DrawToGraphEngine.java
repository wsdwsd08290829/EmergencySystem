package main;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

import org.apache.log4j.Logger;

import models.*;
/**
 * @author sidawang
 */
public class DrawToGraphEngine {
	final static Logger logger = Logger.getLogger(DrawToGraphEngine.class);
	Graphics g;
	// for emergency query, index 0 is for broken aounce system
	public static ArrayList<Color> colorList = new ArrayList<Color>();
	
	/**
	 * Load default colors in the list for use in sequence
	 * @param colorList
	 */
	private static void fillCollorList(ArrayList<Color> colorList) {
		colorList.add(Color.red);
		colorList.add(Color.blue);
		colorList.add(Color.ORANGE);
		colorList.add(Color.magenta);
		colorList.add(Color.pink);
		colorList.add(Color.cyan);
		colorList.add(Color.green);
		colorList.add(Color.white);
	}

	/**
	 * @param g: the Graphic to draw on
	 */
	DrawToGraphEngine(Graphics g) {
		this.g = g;
		fillCollorList(colorList);
	}
	/**
	 * @param image: the image to draw on
	 */
	DrawToGraphEngine(BufferedImage image) {
		this.g = image.createGraphics();
		fillCollorList(colorList);
	}

	/*
	 * draw different shape on image in application window
	 */
	/**
	 * @param center: the center of Rect(the point of target coordination)
	 * @param width
	 * @param height
	 * @param c: Color
	 */
	public void drawRect(Point center, int width, int height, Color c) {
		// as long as do not read image from file, image(buffer) can save upto 
		// now changes
		g.setColor(c);
		g.fillRect(center.x - width / 2, center.y - height / 2, width, height);
	}

	/**
	 * @param center: center of Circle
	 * @param radius
	 * @param c:Color
	 */
	void drawCircle(Point center, int radius, Color c) {
		g.setColor(c);
		g.drawOval(center.x - radius, center.y - radius, radius * 2, radius * 2);
	}

	/**
	 * @param x1  x of Point1
	 * @param y1  y of Point1
	 * @param x2  x of Point2
	 * @param y2  y of Point2
	 * @param c   Color
	 */
	void drawLine(int x1, int y1, int x2, int y2, Color c) {
		g.setColor(c);
		g.drawLine(x1, y1, x2, y2);
	}

	/**
	 * @param pointsX x point Array of int
	 * @param pointsY y point Array of int
	 * @param nPoints number of points in polygon
	 * @param c Color
	 */
	void drawPolygon(int[] pointsX, int[] pointsY, int nPoints, Color c) {
		g.setColor(c);
		g.drawPolygon(pointsX, pointsY, nPoints);
	}

	/*
	 * draw graph list of a feature models
	 */
	
	/**
	 * draw each AnnouncementSystem on image/graph (circle range + center rectangle)
	 * @param announcementsystems :list of Model AnnouncementSystem
	 * @param c
	 */
	public void drawAnnouncementsystems(
			ArrayList<AnnouncementSystem> announcementsystems,
			Color c) {
		logger.info("drawing: announcement systems");
		for (AnnouncementSystem as : announcementsystems) {
			Point center = as.getCenter();
			int radius = as.getRadius();
			drawCircle(center, radius, c);
			drawRect(center, 15, 15, c);
		}
	}

	/**
	 * draw each Building on image/graph (polygon)
	 * @param buildings :list of Model Building
	 * @param c
	 */
	public void drawBuildings(ArrayList<Building> buildings, Color c) {
		logger.info("drawing: buildings");
		for (Building bu : buildings) {
			drawPolygon(bu.getPointsX(), bu.getPointsY(), bu.getPoints().length / 2, c);
		}
	}

	/**
	 * @param students
	 * @param c
	 */
	public void drawStudents(ArrayList<Student> students,
			Color c) {
		logger.info("drawing: students");
		for (Student stu : students) {
			Point center = stu.getPoint();
			drawRect(center, 10, 10, c);
		}
	}
}
