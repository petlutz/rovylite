package de.gnox.rovy.ocv;

import java.util.ArrayList;
import java.util.List;

public class Marker {
	
	private int value;
	
	private List<Point> points = new ArrayList<>(4);
	
	public Marker() {
	}

	public Marker(int value) {
		super();
		this.value = value;
	}

	public int getValue() {
		return value;
	}
	
	public void setValue(int value) {
		this.value = value;
	}

	public List<Point> getPoints() {
		return points;
	}

	public void setPoints(List<Point> points) {
		this.points = points;
	}
	
	@Override
	public String toString() {
		return "value=" + value + ",Points:"
				+ points.stream().map(item -> item.toString()).reduce((a, b) -> a + b ).orElse("");
	}
	
}
