package de.gnox.rovy.ocv;

import java.io.StringWriter;
import java.util.Arrays;

public class Vector3d {

	private double[] vector;

	public Vector3d(double[] vector) {
		if (vector.length != 3)
			throw new IllegalArgumentException();
		this.vector = vector;
	}

	public double getX() {
		return vector[0];
	}

	public void setX(double x) {
		vector[0] = x;
	}

	public double getY() {
		return vector[1];
	}

	public void setY(double y) {
		vector[1] = y;
	}

	public double getZ() {
		return vector[1];
	}

	public void setZ(double z) {
		vector[1] = z;
	}

	@Override
	public String toString() {
		return "(" + Arrays.stream(vector).mapToObj(elm -> String.format("%1.4f", elm)).reduce((a, b) -> a + " " + b)
				.orElse("") + ")";
	}
	
	
	
}
