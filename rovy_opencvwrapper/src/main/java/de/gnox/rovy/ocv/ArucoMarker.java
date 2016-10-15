package de.gnox.rovy.ocv;

import java.util.ArrayList;
import java.util.List;

public class ArucoMarker {
	
	private int id;
	
	private List<Point2i> corners = new ArrayList<>(4);
	
	private Matrix4d rotationMatrix;
	
	private Vector4d translationVector;
	
	private Matrix4d transformationMatrix;
	
	public Matrix4d getRotationMatrix() {
		return rotationMatrix;
	}

	public void setRotationMatrix(Matrix4d rotationVector) {
		this.rotationMatrix = rotationVector;
	}

	public Vector4d getTranslationVector() {
		return translationVector;
	}

	public void setTranslationVector(Vector4d translationVector) {
		this.translationVector = translationVector;
	}
	
	public Matrix4d getTransformationMatrix() {
		if (transformationMatrix == null && translationVector != null && rotationMatrix != null)
			transformationMatrix = Matrix4d.getTranslationMatrix(translationVector).mult(rotationMatrix);
		return transformationMatrix;
	}

	private Point2i center;
	
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

	public List<Point2i> getCorners() {
		return corners;
	}

	public void setCorners(List<Point2i> points) {
		this.corners = points;
	}
	
	@Override
	public String toString() {
		return "value=" + id + ",Points:"
				+ corners.stream().map(item -> item.toString()).reduce((a, b) -> a + b ).orElse("");
	}
	
	public Point2i getCenter() {
		if (center == null) {
			Point2i newCenter = corners.stream()
					.reduce((a,b) -> a.add(b))
					.orElse(new Point2i(0,0));
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
