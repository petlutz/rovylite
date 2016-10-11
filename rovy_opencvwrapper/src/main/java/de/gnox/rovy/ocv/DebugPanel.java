package de.gnox.rovy.ocv;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.awt.image.WritableRaster;

import javax.swing.JPanel;

import org.opencv.core.Mat;

public class DebugPanel extends JPanel {

	private static final long serialVersionUID = 1L;

	public DebugPanel(Mat frame) {
		super();
		setPreferredSize(new Dimension(640, 480));
		setMinimumSize(new Dimension(640, 480));
		setFrame(frame);
	}
	
	@Override
	public void paint(Graphics g) {
		if (mat == null)
			return;
		mat2Image();
		if (image != null)
		   g.drawImage(image, 0, 0, this);

	}

	public void setFrame(Mat currentMat) {
		this.mat = currentMat;
	}
	
	public void mat2Image() {
		if (image == null) {
			int type = 0;
	        if (mat.channels() == 1) {
	            type = BufferedImage.TYPE_BYTE_GRAY;
	        } else if (mat.channels() == 3) {
	            type = BufferedImage.TYPE_3BYTE_BGR;
	        }
	        if (mat.width() == 0 || mat.height() == 0)
	        	return;
	        image = new BufferedImage(mat.width(), mat.height(), type);
		}
		
        WritableRaster raster = image.getRaster();
        DataBufferByte dataBuffer = (DataBufferByte) raster.getDataBuffer();
        byte[] data = dataBuffer.getData();
        mat.get(0, 0, data);
	}
	
	private Mat mat; 
		
	private BufferedImage image;
	

}
