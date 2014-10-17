package models;
import java.awt.Point;


/**
 * @author sidawang
 * AnnouncementSystem model: Id, center, radius
 */
public class AnnouncementSystem {
		private Point center;
		private int radius;
		public String announcementSystemID;

		public AnnouncementSystem(String announcementSystemID, Point center,
				int radius) {
			super();
			this.announcementSystemID = announcementSystemID;
			this.setCenter(center);
			this.setRadius(radius);
		}

		public Point getCenter() {
			return center;
		}

		public void setCenter(Point center) {
			this.center = center;
		}

		public int getRadius() {
			return radius;
		}

		public void setRadius(int radius) {
			this.radius = radius;
		}
	}
