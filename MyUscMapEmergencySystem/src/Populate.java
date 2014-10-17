import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * save students, buildings, announcement systems data to database
 * (database need to be created first in mycreatedb.sql( vs mydropdb.sql)
 * @author sidawang
 *
 */
public class Populate {
	//TODO change index performance, use prepared statement/JParameter?
	public static void main(String[] args) {
		String studentsFileName = "", buildingsFileName = "", announceFileName = "";
		if (args != null && args.length == 3) {
			System.out.println(args);
			studentsFileName = args[0];
			buildingsFileName = args[1];
			announceFileName = args[2];
		} else {
			studentsFileName = "students.xy";
			buildingsFileName = "buildings.xy";
			announceFileName = "announcementSystems.xy";
		}
		try (Connection con = DriverManager.getConnection(
				"jdbc:oracle:thin:@localhost:1521:orcl", "hr", "9280dswdsw");
				Statement stmt = con.createStatement();) {
			try {
				/* parse file to sql, save data to db */
				loadStudents(studentsFileName, con, stmt);
				loadBuildings(buildingsFileName, con, stmt);
				loadAnnounces(announceFileName, con, stmt);
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * loading announcementSystem info to database;
	 */
	private static void loadAnnounces(String filename, Connection con,
			Statement stmt)  throws SQLException, IOException{
		stmt.executeUpdate("delete from myannouncementsystems");
		BufferedReader br = new BufferedReader(new FileReader(filename));
		String line = "";
		while ((line = br.readLine()) != null) {
			String sql = getInsertAnnounceSql(line);
			stmt.executeUpdate(sql);
		}
		br.close();
	}
	private static String getInsertAnnounceSql(String line) {
		String[] parts = line.split(",");
		String announcementSystemID = "'" + parts[0] + "'";  //'p0'
		String center = parts[1] + ", " + parts[2];  // 200,201
		String radius = parts[3];
		String locationArray = getLocationArray(Integer.parseInt(parts[1].trim()), Integer.parseInt(parts[2].trim()), Integer.parseInt(radius.trim()));
		String shape =  "sdo_geometry(2003, null, null, SDO_ELEM_INFO_ARRAY(1,1003,4),  SDO_ORDINATE_ARRAY(" +locationArray+ "))";
		String centerShape = "sdo_geometry(2001, null, sdo_point_type("+ center +", null), null, null)";
		String sql = "insert into myannouncementsystems values (" + announcementSystemID + " , " +  shape + "," + centerShape+","+ Integer.parseInt(radius.trim()) +")";
		System.out.println(sql);
		//eg.insert into myannouncementsystems values ('a1psa' , 
		//sdo_geometry(2003, null, null, SDO_ELEM_INFO_ARRAY(1,1003,4),SDO_ORDINATE_ARRAY(-10, 155, 190, 155, 90, 55)),
		//sdo_geometry(2001, null, sdo_point_type( 90,  155, null), null, null))

		return sql;
	}
	private static String getLocationArray(int centerX, int centerY, int radius) {
		String p1 = (centerX-radius) + ", " + centerY;
		String p2 = (centerX+radius) + ", " + centerY;
		String p3 = centerX + ", " + (centerY-radius);
		String locationArray = p1+", " + p2  + ", " + p3; //1,2,3,4,5,6
		return locationArray;
	}
	
	/**
	 * loading building info to database;
	 */
	private static void loadBuildings(String filename, Connection con,
			Statement stmt)  throws SQLException, IOException{
		stmt.executeUpdate("delete from mybuildings");
		BufferedReader br = new BufferedReader(new FileReader(filename));
		String line = "";
		while ((line = br.readLine()) != null) {
			String sql = getInsertBuldingSql(line);
			stmt.executeUpdate(sql);
		}
		br.close();
	}
	private static String getInsertBuldingSql(String line) {
		String[] parts = line.split(",");
		String buildingID = "'" + parts[0] + "'";  //'b0'
		String buildingName = "'" + parts[1] + "'";  //'PSA'
		String location = "";
		for(int i=3;i<parts.length-1;i++){
			location += parts[i] + ",";
		}
		location += parts[parts.length-1] + "," + parts[3]+ "," + parts[4]; //last point == first point
		/* save to database(when create tables must log in as normal user, not dba) */
		String shape =  "sdo_geometry(2003, null, null, SDO_ELEM_INFO_ARRAY(1,1003,1),  SDO_ORDINATE_ARRAY(" +location+ "))";
		String sql = "insert into mybuildings values (" + buildingID + " , " + buildingName +"," + shape + ")";
		System.out.println(sql);
		//eg. insert into mybuildings values ('b0','PSA', sdo_geometry(2001, null,  null, 
		//sdo_geometry(2003, null, null, SDO_ELEM_INFO_ARRAY(1,1003,1),  
		//SDO_ORDINATE_ARRAY(79, 68, 184, 125, 179, ... , 79, 68)));
		return sql;
	}
	
	/**
	 * loading student info to database;
	 */
	private static void loadStudents(String filename, Connection con, Statement stmt) throws SQLException, IOException {
				/* clear previous data */
				stmt.executeUpdate("delete from mystudents");
				/* read and parse line from file*/
				BufferedReader br = new BufferedReader(new FileReader(filename));
				String line = "";
				while ((line = br.readLine()) != null) {
					String sql = getInsertStudentSql(line);
					stmt.executeUpdate(sql);
				}
				br.close();
	}
	private static String getInsertStudentSql(String line) {
		String[] parts = line.split(",");
		String studentID = "'" + parts[0] + "'";  //'p0'
		String location = parts[1] + ", " + parts[2];  // 200,201
		 
		/* save to database(when create tables must log in as normal user, not dba) */
		String shape =  "sdo_geometry(2001, null, sdo_point_type("+ location +", null), null, null)";
		String sql = "insert into mystudents values (" + studentID + " , " +  shape + ")";
		 //eg. insert into mystudents values ('p0', sdo_geometry(2001, null, sdo_point_type(228, 102, null), null, null));
		System.out.println(sql); 
		return sql;
	}
}
