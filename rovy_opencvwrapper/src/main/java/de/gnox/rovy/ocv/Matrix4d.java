package de.gnox.rovy.ocv;

public class Matrix4d {
	
	private double[][] values;
	
	public Matrix4d(double[][] values) {
		super();
		this.values = values;
	}

	public double[][] getValues() {
		return values;
	}
	
	public Matrix4d mult(Matrix4d other) {
		double[][] mA = values;
		double[][] mB = other.values;
		Matrix4d result = createNullMatrix();
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
 	
	public Vector4d mult(Vector4d vector) {
		double[][] mA = values;
		double[] mB = vector.getValues();
		Vector4d result = Vector4d.createNullVector();
		double[] mR = result.getValues();
		
		for (int i = 0; i < 4; i++) {
			for (int j = 0; j < 4; j++) {
				mR[i] = mR[i] + mA[i][j] * mB[j];
			}
		}
		return result;
	}
	
	public static Matrix4d createNullMatrix() {
		double m[][] = {   
				{ 0.0d, 0.0d, 0.0d, 0.0d },
               	{ 0.0d, 0.0d, 0.0d, 0.0d },
                { 0.0d, 0.0d, 0.0d, 0.0d },
                { 0.0d, 0.0d, 0.0d, 0.0d } 
                };
		return new Matrix4d(m);
	}	
	
	
	public static Matrix4d createIdentityMatrix() {
		double m[][] = {   
				{ 1.0d, 0.0d, 0.0d, 0.0d },
               	{ 0.0d, 1.0d, 0.0d, 0.0d },
                { 0.0d, 0.0d, 1.0d, 0.0d },
                { 0.0d, 0.0d, 0.0d, 1.0d } 
                };
		return new Matrix4d(m);
	}	
	
	public static Matrix4d createRotXMatrix(double a) {
		double m[][] = {   
				{ 1.0d, 0.0d,         0.0d,        0.0d },
               	{ 0.0d, Math.cos(a), -Math.sin(a), 0.0d },
                { 0.0d, Math.sin(a),  Math.cos(a), 0.0d },
                { 0.0d, 0.0d,         0.0d,        1.0d } 
                };
		return new Matrix4d(m);
	}
	
	public static Matrix4d createRotYMatrix(double a) {
		double m[][] = {   
				{ Math.cos(a), 0.0d,  Math.sin(a), 0.0d },
               	{ 0.0d,        1.0d,  0.0d,        0.0d },
                {-Math.sin(a), 0.0d,  Math.cos(a), 0.0d },
                { 0.0d,        0.0d,  0.0d,        1.0d } 
                };
		return new Matrix4d(m);
	}
	
	public static Matrix4d createRotZMatrix(double a) {
		double m[][] = {   
				{ Math.cos(a), -Math.sin(a), 0.0d, 0.0d },
               	{ Math.sin(a),  Math.cos(a), 0.0d, 0.0d },
                { 0.0d,         0.0d,        1.0d, 0.0d },
                { 0.0d,         0.0d,        0.0d, 1.0d } 
                };
		return new Matrix4d(m);
	}
	
	public static Matrix4d createTranslationMatrix(Vector4d v) {
		double m[][] = {   
				{ 1.0d, 0.0d, 0.0d, v.getX() },
               	{ 0.0d, 1.0d, 0.0d, v.getY() },
                { 0.0d, 0.0d, 1.0d, v.getZ() },
                { 0.0d, 0.0d, 0.0d, 1.0d     } 
                };
		return new Matrix4d(m);
	}
	
	
}
