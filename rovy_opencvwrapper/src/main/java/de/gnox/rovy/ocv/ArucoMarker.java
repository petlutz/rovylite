package de.gnox.rovy.ocv;

import java.util.ArrayList;
import java.util.List;

public class ArucoMarker {
	
	private int id;
	
	private List<Point> corners = new ArrayList<>(4);
	
	private Point center;
	
	private Integer size;
	
	public ArucoMarker() {
	}

	public ArucoMarker(int id) {
		super();
		this.id = id;
	}

	public int getId() {
		return id;
	}
	
	public void setId(int id) {
		this.id = id;
	}

	public List<Point> getCorners() {
		return corners;
	}

	public void setCorners(List<Point> points) {
		this.corners = points;
	}
	
	@Override
	public String toString() {
		return "value=" + id + ",Points:"
				+ corners.stream().map(item -> item.toString()).reduce((a, b) -> a + b ).orElse("");
	}
	
	public Point getCenter() {
		if (center == null) {
			Point newCenter = corners.stream()
					.reduce((a,b) -> a.add(b))
					.orElse(new Point(0,0));
			newCenter.mult(1.0f / corners.size());
			center = newCenter;
		}
		return center;
	}
	
	public int getSize() {
		if (size == null) {
			int maxX = corners.stream().map(p -> p.getX()).reduce((a, b) -> a > b ? a : b).orElse(0);
			int minX = corners.stream().map(p -> p.getX()).reduce((a, b) -> a < b ? a : b).orElse(0);
			size = maxX - minX;
		}
		return size;
	}
	
}
