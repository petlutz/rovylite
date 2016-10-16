package de.gnox.rovy.ocv;

import java.util.Arrays;

public class Vector {

	private double[] values;

	public Vector(double[] values) {
		super();
		this.values = values;
	}

	public Vector(double x, double y, double z) {
		this.values = new double[4];
		this.values[0] = x;
		this.values[1] = y;
		this.values[2] = z;
		this.values[3] = 1.0d;
	}

	public double getX() {
		return values[0];
	}

	public void setX(double x) {
		values[0] = x;
	}

	public double getY() {
		return values[1];
	}

	public void setY(double y) {
		values[1] = y;
	}

	public double getZ() {
		return values[2];
	}

	public void setZ(double z) {
		values[2] = z;
	}

	public double[] getValues() {
		return values;
	}
	
	public double getAngleBetween(Vector other) {
		double x1 = getX();
		double y1 = getY();
		double z1 = getZ();
		double x2 = other.getX();
		double y2 = other.getY();
		double z2 = other.getZ();
		double cos = (x1 * x2 + y1 * y2 + z1 * z2)
				/ (Math.sqrt(x1 * x1 + y1 * y1 + z1 * z1) * Math.sqrt(x2 * x2 + y2 * y2 + z2 * z2));
		return Math.acos(cos);

	}
	
	public double getAngleBetweenAsDeg(Vector other) {
		double rad = getAngleBetween(other);
		double deg = rad * (double)360 / ((double)2 * Math.PI);
		return deg;
	}
	
	public Vector subtract(Vector other) {
		double[] valuesNew = {
			getX() - other.getX(),
			getY() - other.getY(),
			getZ() - other.getZ(),
			1.0d
		};
		return new Vector(valuesNew);
	}
	
	public double getLength() {
		double result = 0.0f;
		for (double val : values)
			result += val * val;
		return Math.sqrt(result);
	}

	
	public static Vector createNullVector() {
		double v[] = { 0.0d, 0.0d, 0.0d, 0.0d };
		return new Vector(v);
	}	
	
	@Override
	public String toString() {
		return "(" + Arrays.stream(values).mapToObj(elm -> String.format("%1.4f", elm)).reduce((a, b) -> a + " " + b)
				.orElse("") + ")";
	}
	
	
	
}
