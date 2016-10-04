package de.gnox.rovy.ocv;

public class Point {
	
	private int x;
	
	private int y;
	
	public Point() {
	}

	public Point(int x, int y) {
		super();
		this.x = x;
		this.y = y;
	}

	public int getX() {
		return x;
	}

	public void setX(int x) {
		this.x = x;
	}

	public int getY() {
		return y;
	}

	public void setY(int y) {
		this.y = y;
	}
	
	public Point add(Point other) {
		x += other.x;
		y += other.y;
		return this;
	}
	
	public Point mult(float factor) {
		x *= factor;
		y *= factor;
		return this;
	}
	
	@Override
	public String toString() {
		return "(x=" + x + ",y=" + y + ")";
	}

}
