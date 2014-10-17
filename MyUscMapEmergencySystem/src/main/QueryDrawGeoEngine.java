package main;

import java.awt.Color;
import java.awt.Point;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import models.AnnouncementSystem;
import models.Building;
import models.Student;
import oracle.spatial.geometry.JGeometry;
import oracle.sql.STRUCT;

/**
 * @author sidawang
 * @version 1.2 refactor code, more modularity
 * @version 1.3 add logging system, javaDoc
 */
public class QueryDrawGeoEngine {
	final static Logger logger = Logger.getLogger(QueryDrawGeoEngine.class);
	// draw engine to draw feature to image
	DrawToGraphEngine dge;

	// save list of features from query
	ArrayList<Student> students;
	ArrayList<AnnouncementSystem> announcementsystems;
	ArrayList<Building> buildings;

	// init draw eigine and lists
	public QueryDrawGeoEngine(DrawToGraphEngine dge) {
		students = new ArrayList<Student>();
		announcementsystems = new ArrayList<AnnouncementSystem>();
		buildings = new ArrayList<Building>();
		this.dge = dge;
	}

	/*
	 * execute different query and draw result to image using dge based on
	 * feature type of the query;
	 */
	/**
	 * save students from query to students(list)
	 * 
	 * @param sql
	 *            : sql for query students
	 */
	public void queryStudents(String sql) {
		logger.info(sql);
		try (Connection con = DriverManager.getConnection(
				"jdbc:oracle:thin:@localhost:1521:orcl", "hr", "9280dswdsw");
				Statement stmt = con.createStatement();) {
			ResultSet rs = stmt.executeQuery(sql);
			while (rs.next()) {
				STRUCT st = (oracle.sql.STRUCT) rs.getObject("shape");
				JGeometry j_geom = JGeometry.load(st);

				Point stuPoint = new Point((int) j_geom.getJavaPoint().getX(),
						(int) j_geom.getJavaPoint().getY());
				// dge.drawRect(stuPoint, 10, 10, c);
				students.add(new Student(rs.getString("studentID"), stuPoint));
			}
		} catch (SQLException ex) {
			ex.printStackTrace();
		}
	}

	/**
	 * save building to buildings' list
	 * 
	 * @param sql
	 *            : query for Buildings
	 */
	public void queryBuildings(String sql) {
		logger.info(sql);
		try (Connection con = DriverManager.getConnection(
				"jdbc:oracle:thin:@localhost:1521:orcl", "hr", "9280dswdsw");
				Statement stmt = con.createStatement();) {
			ResultSet rs = stmt.executeQuery(sql);
			while (rs.next()) {
				STRUCT st = (oracle.sql.STRUCT) rs.getObject("shape");
				JGeometry j_geom = JGeometry.load(st);
				double[] pointArr = j_geom.getOrdinatesArray();
				String buildingID = rs.getString("buildingID");
				buildings.add(new Building(buildingID, pointArr));

				// dge.drawPolygon(pointsX, pointsY, j_geom.getNumPoints(), c);
			}
		} catch (SQLException ex) {
			ex.printStackTrace();
		}
	}

	/**
	 * save result to announcementsystems' list
	 * 
	 * @param sql
	 *            : query for announce system
	 */
	public void queryAnnouncementSystem(String sql) {
		logger.info(sql);
		try (Connection con = DriverManager.getConnection(
				"jdbc:oracle:thin:@localhost:1521:orcl", "hr", "9280dswdsw");
				Statement stmt = con.createStatement();) {
			ResultSet rs = stmt.executeQuery(sql);
			while (rs.next()) {
				STRUCT st = (oracle.sql.STRUCT) rs.getObject("shape");
				JGeometry j_geom = JGeometry.load(st);
				STRUCT ct = (oracle.sql.STRUCT) rs.getObject("center");
				int radius = rs.getInt("radius");
				JGeometry j_geom1 = JGeometry.load(ct);
				Point center = new Point((int) j_geom1.getJavaPoint().getX(),
						(int) j_geom1.getJavaPoint().getY());
				// System.out.println((int)j_geom.getJavaPoint().getX()+ " " +
				// (int)j_geom.getJavaPoint().getY());

				// dge.drawCircle(center, radius, c);
				// dge.drawRect(center, 15, 15, c);
				announcementsystems.add(new AnnouncementSystem(rs
						.getString("announcementSystemID"), center, radius));
				// res.add(pointCord);
			}
		} catch (SQLException ex) {
			ex.printStackTrace();
		}
	}

	/*
	 * construct sql for different query method; query and save result to list
	 * draw each item in list to image
	 */
	/**
	 * perform queryWhole method display all student, announceSystem or
	 * buildings on image
	 * 
	 * @param featureMap
	 *            : map method to boolean, indicate whether query for this
	 *            feature or not
	 * @return list of query to be log in textArea
	 */
	public ArrayList<String> queryWhole(Map<String, Boolean> featureMap) {
		String sql = "";
		ArrayList<String> queryLog = new ArrayList<String>();
		for (String feature : featureMap.keySet()) {
			if (featureMap.get(feature)) {
				switch (feature) {
				case "myannouncementsystems":
					sql = "select * from myannouncementsystems";
					queryLog.add(sql + "\n");
					
					queryAnnouncementSystem(sql);
					dge.drawAnnouncementsystems(announcementsystems, Color.red);
					break;
				case "mystudents":
					sql = "select s.shape, s.studentID from mystudents s";
					queryLog.add(sql + "\n");
					queryStudents(sql);
					dge.drawStudents(students, Color.green);
					break;
				case "mybuildings":
					sql = "select b.shape,b.buildingID from mybuildings b";
					queryLog.add(sql + "\n");
					queryBuildings(sql);
					dge.drawBuildings(buildings, Color.yellow);
					break;
				}
			}
		}
		return queryLog;
	}

	/**
	 * perform query Point method query within circle with
	 * center(queryPointCenter), radius(queryPointRadius) for student, building
	 * or announce system, mark them green on image. mark yellow if feature
	 * active is closest to center
	 * 
	 * @param featureMap
	 *            : map method to boolean, indicate whether query for this
	 *            feature or not
	 * @param queryPointCenter
	 *            : center of point
	 * @param queryPointRadius
	 *            : range of query around point
	 * @return list of query to be log in textArea
	 */
	public ArrayList<String> queryPoint(Map<String, Boolean> featureMap,
			Point queryPointCenter, int queryPointRadius) {
		ArrayList<String> queryLog = new ArrayList<String>();
		// queryGeo is circle with radius 50;
		String queryGeo = ""; // circle for related
		queryGeo = "sdo_geometry(2003, NULL, NULL, sdo_elem_info_array(1, 1003, 4), sdo_ordinate_array(";
		queryGeo += (queryPointCenter.x - queryPointRadius) + ","
				+ queryPointCenter.y + ", ";
		queryGeo += (queryPointCenter.x + queryPointRadius) + ","
				+ queryPointCenter.y + ", ";
		queryGeo += queryPointCenter.x + ", "
				+ (queryPointCenter.y + queryPointRadius);
		queryGeo += "))"; // , 'mask=anyinteract')='TRUE'
		String queryGeo2 = ""; // point for nearest
		String point = queryPointCenter.x + ", " + queryPointCenter.y
				+ ", NULL";
		queryGeo2 = "sdo_geometry(2001, NULL, sdo_point_type(" + point
				+ "), NULL,NULL)";
		// imageLabel.setIcon(new ImageIcon(image));
		String select = "";
		String from = "";
		String where = "";
		String sql = "";
		for (String feature : featureMap.keySet()) {
			if (featureMap.get(feature)) {
				if (feature.equals("myannouncementsystems")) {
					select = "* "; // a.center, a.shape, a.radius
					from = "myannouncementsystems a ";
					// queryGeo:sdo_geometry(2003, NULL, NULL,
					// sdo_elem_info_array(1, 1003, 4),
					// sdo_ordinate_array(630,337, 730,337, 680, 387))
					where = "sdo_relate(a.shape, " + queryGeo
							+ ", 'mask=anyinteract')='TRUE'";
					sql = "select " + select + " from " + from + " where "
							+ where;

					queryLog.add(sql);
					queryAnnouncementSystem(sql);
					dge.drawAnnouncementsystems(announcementsystems,
							Color.green);
					// query for nearest point of this feature
					// current use a.center to get nearest announceSystem
					// center;
					// change a.shape to get the closest announceSystem(by
					// perimeter/circumference)
					where += " AND sdo_nn(a.center, " + queryGeo2
							+ ",'sdo_num_res=1') = 'TRUE'";
					sql = "select " + select + " from " + from + " where "
							+ where;
					queryLog.add(sql);
					announcementsystems.clear();
					queryAnnouncementSystem(sql);
					dge.drawAnnouncementsystems(announcementsystems,
							Color.yellow);
				}
				if (feature.equals("mystudents")) {
					select = "s.shape, s.studentID ";
					from = "mystudents s";
					where = "sdo_relate(s.shape, " + queryGeo
							+ ", 'mask=anyinteract')='TRUE'";
					sql = "select " + select + " from " + from + " where "
							+ where;
					queryLog.add(sql);
					queryStudents(sql);
					dge.drawStudents(students, Color.green);
					where += "AND sdo_nn(s.shape, " + queryGeo2
							+ ",'sdo_num_res=1') = 'TRUE'";
					sql = "select " + select + " from " + from + " where "
							+ where;
					queryLog.add(sql);
					students.clear();
					queryStudents(sql);
					dge.drawStudents(students, Color.yellow);
				}
				if (feature.equals("mybuildings")) {
					select = " b.shape, b.buildingID ";
					from = " mybuildings b ";
					where = " sdo_relate(b.shape, " + queryGeo
							+ ", 'mask=anyinteract')='TRUE'";
					sql = "select " + select + " from " + from + " where "
							+ where;
					queryLog.add(sql);
					queryBuildings(sql);
					dge.drawBuildings(buildings, Color.green);
					where += "AND sdo_nn(b.shape, " + queryGeo2
							+ ",'sdo_num_res=1') = 'TRUE'";
					sql = "select " + select + " from " + from + " where "
							+ where;
					queryLog.add(sql);
					buildings.clear();
					queryBuildings(sql);
					dge.drawBuildings(buildings, Color.yellow);

				}
			}
		}

		return queryLog;
	}

	/**
	 * sequence of left click for range (end with right click) to perform query
	 * this function will query for all active features(student, announc system,
	 * buildings) within the range specified by points array
	 * 
	 * @param featureMap
	 * @param pointsList
	 *            : list of range selected by mouse click(start == end according
	 *            to geo query spec)
	 * @return
	 */
	public ArrayList<String> queryRange(Map<String, Boolean> featureMap,
			ArrayList<Point> pointsList) {
		ArrayList<String> queryLog = new ArrayList<String>();
		StringBuilder ordinates = new StringBuilder();
		for (Point p : pointsList) {
			ordinates.append(p.x);
			ordinates.append(",");
			ordinates.append(p.y);
			ordinates.append(" ,");
		}
		String ordinatesString = ordinates.substring(0, ordinates.length() - 1); // remove
																					// last
																					// ","
		// polygon interaction
		String queryGeo = "sdo_geometry(2003, NULL, NULL, sdo_elem_info_array(1, 1003, 1), sdo_ordinate_array(";
		queryGeo += ordinatesString + "))";
		String select = "";
		String from = "";
		String where = "";
		String sql = "";
		for (String feature : featureMap.keySet()) {
			if (featureMap.get(feature)) {
				if (feature.equals("myannouncementsystems")) {
					select = "* "; // a.center, a.shape, a.radius
					from = "myannouncementsystems a ";
					// queryGeo:sdo_geometry(2003, NULL, NULL,
					// sdo_elem_info_array(1, 1003, 4),
					// sdo_ordinate_array(630,337, 730,337, 680, 387))
					where = "sdo_relate(a.shape, " + queryGeo
							+ ", 'mask=anyinteract')='TRUE'";
					sql = "select " + select + " from " + from + " where "
							+ where;
					queryLog.add(sql);
					queryAnnouncementSystem(sql);
					dge.drawAnnouncementsystems(announcementsystems, Color.red);
				}
				if (feature.equals("mystudents")) {
					select = "s.shape, s.studentID ";
					from = "mystudents s";
					where = "sdo_relate(s.shape, " + queryGeo
							+ ", 'mask=anyinteract')='TRUE'";
					sql = "select " + select + " from " + from + " where "
							+ where;
					queryLog.add(sql);
					queryStudents(sql);
					dge.drawStudents(students, Color.green);
				}
				if (feature.equals("mybuildings")) {
					select = " b.shape, b.buildingID ";
					from = " mybuildings b ";
					where = " sdo_relate(b.shape, " + queryGeo
							+ ", 'mask=anyinteract')='TRUE'";
					sql = "select " + select + " from " + from + " where "
							+ where;
					queryLog.add(sql);
					queryBuildings(sql);
					dge.drawBuildings(buildings, Color.yellow);
				}
			} // if feature active
		} // loop for feature end
		return queryLog;
	} // func end

	/**
	 * query all students within range of the announce system
	 * 
	 * @param queryPointCenter
	 *            : the center of announce system
	 * @return List of Log message of query
	 */
	public ArrayList<String> querySurroundAnnounce(Point queryPointCenter) {
		ArrayList<String> queryLog = queryNearestAnnounce(queryPointCenter);
		/*
		 * dge.drawCircle(as.center, as.radius, Color.red);
		 * dge.drawRect(as.center, 15, 15, Color.red);
		 */
		// get and draw students within the announce system
		int x = announcementsystems.get(0).getCenter().x;
		int y = announcementsystems.get(0).getCenter().y;
		int radius = announcementsystems.get(0).getRadius();
		String queryGeo = "SDO_geometry(2001, NULL, SDO_point_type(" + x + ","
				+ y + ",NULL), NULL,NULL)";
		String sql = "select s.shape, s.studentID from mystudents s where sdo_within_distance(s.shape, "
				+ queryGeo + ", 'distance=" + radius + "') = 'TRUE'";
		queryStudents(sql);
		dge.drawStudents(students, Color.green);
		queryLog.add(sql);
		return queryLog;
	}

	/**
	 * query for the nearest AnnounceSystem from the point clicked
	 * 
	 * @param queryPointCenter
	 *            : the point clicked on image
	 * @return List of Log message of query
	 */
	public ArrayList<String> queryNearestAnnounce(Point queryPointCenter) {
		ArrayList<String> queryLog = new ArrayList<String>();
		// get nearest announce system
		String queryGeo = "SDO_geometry(2001, NULL, SDO_point_type("
				+ queryPointCenter.x + "," + queryPointCenter.y
				+ ",NULL), NULL,NULL)";
		String sql = "select * from myannouncementsystems a where sdo_nn(a.shape, "
				+ queryGeo + ",  'sdo_num_res=1') = 'TRUE'";
		queryAnnouncementSystem(sql);
		dge.drawAnnouncementsystems(announcementsystems, Color.red);
		queryLog.add(sql);
		return queryLog;
	}

	/**
	 * first, find nearest announce system of queryPointCenter then for each
	 * student within the announce system assign him the second nearest system
	 * (simulate the covering system is broken)
	 * @param queryPointCenter
	 *            point by mouse click
	 * @return
	 */
	public ArrayList<String> queryEmergency(Point queryPointCenter) {
		ArrayList<String> queryLog = querySurroundAnnounce(queryPointCenter);
		String sql = "";
		Student stu = null;
		AnnouncementSystem announce = announcementsystems.get(0);
		// nearest annouce system info (break down)

		String id = announcementsystems.get(0).announcementSystemID;
		int colorIndex = 1;

		// map the second nearest announce system to the color assigned to it
		Map<String, Color> announceSystemSelected = new HashMap<String, Color>();
		announceSystemSelected.put(id, Color.red);

		queryAndDrawAnnouncementSystemExceptID(id, announceSystemSelected);

		logger.info("student size within broken emergency range: " + students.size());
		for (int i = 0; i < students.size(); i++) {
			stu = students.get(i);
			String queryGeo = "SDO_geometry(2001, NULL, SDO_point_type("
					+ stu.getPoint().x + "," + stu.getPoint().y
					+ ",NULL), NULL,NULL)";
			sql = "select * from myannouncementsystems a where sdo_nn(a.shape, "
					+ queryGeo
					+ ",  'sdo_num_res=2') = 'TRUE' AND a.announcementSystemID != '"
					+ id + "'";
			logger.info(sql);
			colorIndex = queryAndDrawResignStudentToAnnouncementSystem(sql,
					colorIndex, announceSystemSelected, stu);
			announce = announcementsystems.get(0);
			queryLog.add(sql);

		}
		return queryLog;
	}

	/*
	 * helper method for queryEmmergency find all AnnouncementSystem not with id
	 */
	private void queryAndDrawAnnouncementSystemExceptID(String id,
			Map<String, Color> announceSystemSelected) {
		int colorIndex = 1;
		String sql = "select * from myannouncementsystems a where a.announcementSystemID != '"
				+ id + "'";
		logger.info(sql);
		try (Connection con = DriverManager.getConnection(
				"jdbc:oracle:thin:@localhost:1521:orcl", "hr", "9280dswdsw");
				Statement stmt = con.createStatement();) {
			ResultSet rs = stmt.executeQuery(sql);
			while (rs.next()) {
				/*
				 * STRUCT st = (oracle.sql.STRUCT) rs.getObject("shape");
				 * JGeometry j_geom = JGeometry.load(st);
				 */
				STRUCT ct = (oracle.sql.STRUCT) rs.getObject("center");
				int radius = rs.getInt("radius");
				JGeometry j_geom1 = JGeometry.load(ct);
				Point center = new Point((int) j_geom1.getJavaPoint().getX(),
						(int) j_geom1.getJavaPoint().getY());
				// System.out.println((int)j_geom.getJavaPoint().getX()+ " " +
				// (int)j_geom.getJavaPoint().getY());

				dge.drawCircle(center, radius,
						DrawToGraphEngine.colorList.get(colorIndex));
				dge.drawRect(center, 15, 15,
						DrawToGraphEngine.colorList.get(colorIndex));
				announceSystemSelected.put(
						rs.getString("announcementSystemID"),
						DrawToGraphEngine.colorList.get(colorIndex));
				colorIndex++;
				announcementsystems.add(new AnnouncementSystem(rs
						.getString("announcementSystemID"), center, radius));
				// res.add(pointCord);
			}
		} catch (SQLException ex) {
			ex.printStackTrace();
		}
	}

	// overloading function for emergency query
	private int queryAndDrawResignStudentToAnnouncementSystem(String sql,
			int colorIndex, Map<String, Color> announceSystemSelected,
			Student stu) {
		logger.info(sql);
		try (Connection con = DriverManager.getConnection(
				"jdbc:oracle:thin:@localhost:1521:orcl", "hr", "9280dswdsw");
				Statement stmt = con.createStatement();) {
			ResultSet rs = stmt.executeQuery(sql);
			while (rs.next()) {
				// STRUCT st = (oracle.sql.STRUCT) rs.getObject("shape");
				// JGeometry j_geom = JGeometry.load(st);
				STRUCT ct = (oracle.sql.STRUCT) rs.getObject("center");
				int radius = rs.getInt("radius");
				JGeometry j_geom1 = JGeometry.load(ct);
				Point center = new Point((int) j_geom1.getJavaPoint().getX(),
						(int) j_geom1.getJavaPoint().getY());
				// System.out.println((int)j_geom.getJavaPoint().getX()+ " " +
				// (int)j_geom.getJavaPoint().getY());
				String announceID = rs.getString("announcementSystemID");
				//a new assigned System find, assign it 
				if (!announceSystemSelected.containsKey(announceID)) {
					dge.drawCircle(center, radius,
							DrawToGraphEngine.colorList.get(colorIndex));
					dge.drawRect(center, 15, 15,
							DrawToGraphEngine.colorList.get(colorIndex));
					dge.drawRect(stu.getPoint(), 10, 10,
							DrawToGraphEngine.colorList.get(colorIndex));
					announceSystemSelected.put(announceID,
							DrawToGraphEngine.colorList.get(colorIndex));
					return colorIndex + 1;
				}
				//assign student with same color as new nearest announce system found 
				dge.drawRect(stu.getPoint(), 10, 10,
						announceSystemSelected.get(announceID));
				// announcementsystems.add(new AnnouncementSystem(announceID,
				// center, radius));
				// res.add(pointCord);
			}
		} catch (SQLException ex) {
			ex.printStackTrace();
		}
		return colorIndex;
	}

}
