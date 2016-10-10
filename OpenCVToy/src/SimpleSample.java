import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.awt.image.WritableRaster;

import javax.swing.JFrame;
import javax.swing.JPanel;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.videoio.VideoCapture;

public class SimpleSample extends JPanel {

	static {
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
	}
	
	public SimpleSample() {
		super();
		setPreferredSize(new Dimension(640, 480));
		setMinimumSize(new Dimension(640, 480));
	}
	
	@Override
	public void paint(Graphics g) {
		if (mat == null)
			return;
		
		//Image img = matToBufferedImage(currentMat);
		mat2Image();
		if (image != null)
		   g.drawImage(image, 0, 0, this);

	}

	public static void main(String[] args) {
		

	    VideoCapture camera = new VideoCapture(0);
	    camera.open(0); //Useless
	    if(!camera.isOpened()){
	        System.out.println("Camera Error");
	        System.exit(1);
	    }
	    else{
	        System.out.println("Camera OK?");
	    }

	    SimpleSample toy = new SimpleSample();
	    JFrame f = new JFrame();
	    f.setLayout(new BorderLayout());
	    f.add(toy, BorderLayout.CENTER);
	    f.pack();
	    f.setSize(640, 480);
	    f.setVisible(true);
	    
	    
	    
	    
	    Mat frame = new Mat();
	    toy.setMat(frame);
	    //camera.grab();
	    //System.out.println("Frame Grabbed");
	    //camera.retrieve(frame);
	    //System.out.println("Frame Decoded");

	    
	    while (true) {
	    
	    	//long start = System.currentTimeMillis();
	    	camera.read(frame);
	    	//long duration = System.currentTimeMillis() - start;
	    	//System.out.println(duration + " ms");
	    	
	    	//System.out.println("Frame Obtained");

	    	toy.repaint();
	   
	    	try {
				Thread.sleep(1);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	    }
	    
	    //camera.release();
	   

	    //System.out.println("Captured Frame Width " + frame.width());

//	    Highgui.imwrite("camera.jpg", frame);
	    //System.out.println("OK");
		
//		System.out.println("Welcome to OpenCV " + Core.VERSION);
//		Mat m = new Mat(5, 10, CvType.CV_8UC1, new Scalar(0));
//		System.out.println("OpenCV Mat: " + m);
//		Mat mr1 = m.row(1);
//		mr1.setTo(new Scalar(1));
//		Mat mc5 = m.col(5);
//		mc5.setTo(new Scalar(5));
//		System.out.println("OpenCV Mat data:\n" + m.dump());
	}
	
	
	public void setMat(Mat currentMat) {
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
	
//	public void updateImageByMat(Mat frame) {
//        
//		
//
//        
//
//	}
//	
//    public static BufferedImage matToBufferedImage(Mat frame) {
//        //Mat() to BufferedImage
//        int type = 0;
//        if (frame.channels() == 1) {
//            type = BufferedImage.TYPE_BYTE_GRAY;
//        } else if (frame.channels() == 3) {
//            type = BufferedImage.TYPE_3BYTE_BGR;
//        }
//        BufferedImage image = new BufferedImage(frame.width(), frame.height(), type);
//        WritableRaster raster = image.getRaster();
//        DataBufferByte dataBuffer = (DataBufferByte) raster.getDataBuffer();
//        byte[] data = dataBuffer.getData();
//        frame.get(0, 0, data);
//
//        return image;
//    }

}
