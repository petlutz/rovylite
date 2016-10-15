package de.gnox.rovy.ocv;

import java.util.Arrays;

public class Vector4d {

	private double[] values;

	public Vector4d(double[] values) {
		super();
		this.values = values;
	}

	public Vector4d(double x, double y, double z) {
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
	
	
	public double getLength() {
		double result = 0.0f;
		for (double val : values)
			result += val * val;
		return Math.sqrt(result);
	}

	
	public static Vector4d createNullVector() {
		double v[] = { 0.0d, 0.0d, 0.0d, 0.0d };
		return new Vector4d(v);
	}	
	
	@Override
	public String toString() {
		return "(" + Arrays.stream(values).mapToObj(elm -> String.format("%1.4f", elm)).reduce((a, b) -> a + " " + b)
				.orElse("") + ")";
	}
	
	
	
}
