package de.gnox.rovy.ocv;

public class Matrix {
	
	private double[][] values;
	
	public Matrix(double[][] values) {
		super();
		this.values = values;
	}

	public double[][] getValues() {
		return values;
	}
	
	public Matrix mult(Matrix other) {
		double[][] mA = values;
		double[][] mB = other.values;
		Matrix result = getNullMatrix();
		double[][] mR = result.getValues();
		
		for (int i = 0; i < 4; i++) {
			for (int k = 0; k < 4; k++) {
				for (int j = 0; j < 4; j++) {
					mR[i][k] = mR[i][k] + mA[i][j] * mB[j][k];
				}
			}
		}
		return result;
	}
 	
	public Vector mult(Vector vector) {
		double[][] mA = values;
		double[] mB = vector.getValues();
		Vector result = Vector.createNullVector();
		double[] mR = result.getValues();
		
		for (int i = 0; i < 4; i++) {
			for (int j = 0; j < 4; j++) {
				mR[i] = mR[i] + mA[i][j] * mB[j];
			}
		}
		return result;
	}
	
	public static Matrix getNullMatrix() {
		double m[][] = {   
				{ 0.0d, 0.0d, 0.0d, 0.0d },
               	{ 0.0d, 0.0d, 0.0d, 0.0d },
                { 0.0d, 0.0d, 0.0d, 0.0d },
                { 0.0d, 0.0d, 0.0d, 0.0d } 
                };
		return new Matrix(m);
	}	
	
	
	public static Matrix getIdentityMatrix() {
		double m[][] = {   
				{ 1.0d, 0.0d, 0.0d, 0.0d },
               	{ 0.0d, 1.0d, 0.0d, 0.0d },
                { 0.0d, 0.0d, 1.0d, 0.0d },
                { 0.0d, 0.0d, 0.0d, 1.0d } 
                };
		return new Matrix(m);
	}	
	
	public static Matrix getRotXMatrix(double a) {
		double m[][] = {   
				{ 1.0d, 0.0d,         0.0d,        0.0d },
               	{ 0.0d, Math.cos(a), -Math.sin(a), 0.0d },
                { 0.0d, Math.sin(a),  Math.cos(a), 0.0d },
                { 0.0d, 0.0d,         0.0d,        1.0d } 
                };
		return new Matrix(m);
	}
	
	public static Matrix getRotYMatrix(double a) {
		double m[][] = {   
				{ Math.cos(a), 0.0d,  Math.sin(a), 0.0d },
               	{ 0.0d,        1.0d,  0.0d,        0.0d },
                {-Math.sin(a), 0.0d,  Math.cos(a), 0.0d },
                { 0.0d,        0.0d,  0.0d,        1.0d } 
                };
		return new Matrix(m);
	}
	
	public static Matrix getRotZMatrix(double a) {
		double m[][] = {   
				{ Math.cos(a), -Math.sin(a), 0.0d, 0.0d },
               	{ Math.sin(a),  Math.cos(a), 0.0d, 0.0d },
                { 0.0d,         0.0d,        1.0d, 0.0d },
                { 0.0d,         0.0d,        0.0d, 1.0d } 
                };
		return new Matrix(m);
	}
	
	public static Matrix getTranslationMatrix(Vector v) {
		double m[][] = {   
				{ 1.0d, 0.0d, 0.0d, v.getX() },
               	{ 0.0d, 1.0d, 0.0d, v.getY() },
                { 0.0d, 0.0d, 1.0d, v.getZ() },
                { 0.0d, 0.0d, 0.0d, 1.0d     } 
                };
		return new Matrix(m);
	}
	
	
}
