package models;

/**
 * @author sidawang Building model: list of points represent building polygon,
 *         id
 */
public class Building {
	String buildingID;
	private double[] points;
	private int[] pointsX;
	private int[] pointsY;

	public Building(String buildingID, double[] points) {
		this.buildingID = buildingID;
		this.setPoints(points);
		// construct pointsX pointsY array from points, used for drawing polygon
		setPointsX(new int[points.length / 2]);
		setPointsY(new int[points.length / 2]);
		for (int i = 0; i < points.length; i++) {
			if (i % 2 == 0) {
				getPointsX()[i / 2] = (int) points[i];
			} else {
				getPointsY()[i / 2] = (int) points[i];
			}
		}
	}

	public int[] getPointsX() {
		return pointsX;
	}

	public void setPointsX(int[] pointsX) {
		this.pointsX = pointsX;
	}

	public int[] getPointsY() {
		return pointsY;
	}

	public void setPointsY(int[] pointsY) {
		this.pointsY = pointsY;
	}

	public double[] getPoints() {
		return points;
	}

	public void setPoints(double[] points) {
		this.points = points;
	}
}