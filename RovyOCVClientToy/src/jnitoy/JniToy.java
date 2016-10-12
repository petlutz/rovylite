package jnitoy;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Graphics;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JPanel;

import de.gnox.rovy.ocv.ArucoDictionary;
import de.gnox.rovy.ocv.ArucoMarker;
import de.gnox.rovy.ocv.CameraProcessor;
import de.gnox.rovy.ocv.Matrix4d;
import de.gnox.rovy.ocv.Vector4d;

public class JniToy extends JPanel {

	public List<Vector4d> points = new ArrayList<>();
	
   public JniToy() {
		setPreferredSize(new Dimension(640, 480));
		setMinimumSize(new Dimension(640, 480));
   }
   
	@Override
	public void paint(Graphics g) {
		super.paint(g);
		for (int i = 0; i < points.size(); i++)
			drawPoint(g, i, points.get(i)); 
		
	}
	
	private void drawPoint(Graphics g, int idx, Vector4d p) {
		
		double x2d = 320.0d + (p.getX() * 320.0d);
		double y2d = p.getZ() * 240.0d;
		
		g.drawOval((int)x2d, (int)y2d, 3, 3);
		g.drawString(""+idx, (int)x2d, (int)y2d);
		
	}

	public static void main(String[] args) {
		
		JniToy toy= new JniToy();
		JFrame f = new JFrame();
	    f.setLayout(new BorderLayout());
	    f.add(toy, BorderLayout.CENTER);
	    f.pack();
	    f.setSize(640, 480);
	    f.setVisible(true);
		
		CameraProcessor detector = new CameraProcessor(true, 0);
		detector.initArucoWithPoseEstimation(ArucoDictionary.DICT_4X4_250, 0.1f);
		detector.startCapturing();
		
		while (true) {
			Collection<ArucoMarker> markers = detector.detectArucoMarkers();	
			toy.points.clear();

			for (ArucoMarker marker : markers) {
				
				Vector4d transVec = marker.getTranslationVector();
				Matrix4d rotMatrix = marker.getRotationMatrix();
				Matrix4d tMatrix = Matrix4d.createTranslationMatrix(transVec);
				
				
				Vector4d p0 = new Vector4d(0f, 0, 0);
				Vector4d p1 = new Vector4d(0, 0, 0.2f);
		
				p0 = rotMatrix.mult(p0);
				p0 = tMatrix.mult(p0);

				p1 = rotMatrix.mult(p1);
				p1 = tMatrix.mult(p1);

				
				toy.points.add(p0);
				toy.points.add(p1);
		
				//System.out.println(p1+"");
			}
		
			toy.repaint();
	
			try {
				Thread.sleep(1);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
		
		
		
	}
}
