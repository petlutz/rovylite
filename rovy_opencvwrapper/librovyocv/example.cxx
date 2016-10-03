#include <opencv2/highgui.hpp>
#include <opencv2/opencv.hpp>
#include <opencv2/aruco.hpp>

using namespace std;
using namespace cv;

int main(int, char**)
{
    VideoCapture cap(0); // open the default camera
    if(!cap.isOpened())  // check if we succeeded
        return -1;

    //Mat edges;
    namedWindow("edges",WINDOW_NORMAL);
    
    while(cap.grab()) 
    {
        Mat frame;
	cap.retrieve(frame);
     //   cap >> frame; // get a new frame from camera

	 
	vector< int > markerIds; 
	vector< vector<Point2f> > markerCorners, rejectedCandidates; 

	Ptr<aruco::DetectorParameters> parameters = aruco::DetectorParameters::create(); 
	parameters->doCornerRefinement = true;
	Ptr<aruco::Dictionary> dictionary = aruco::getPredefinedDictionary(aruco::DICT_4X4_250);
	aruco::detectMarkers(frame, dictionary, markerCorners, markerIds, parameters, rejectedCandidates);
	


	if (markerIds.size() > 0) {
		/*
		Mat cameraMatrix, distCoeffs;
		vector< Vec3d > rvecs, tvecs; 
		aruco::estimatePoseSingleMarkers(markerCorners, 0.05, cameraMatrix, distCoeffs, rvecs, tvecs);
		*/

	 	aruco::drawDetectedMarkers(frame, markerCorners, markerIds);
		//aruco::drawAxis(frame, cameraMatrix, distCoeffs, rvecs, tvecs, 0.1);
	}


	for (int markerIdx=0; markerIdx<markerIds.size(); markerIdx++) {
		int markerId = markerIds.at(markerIdx);
		vector<Point2f> points = markerCorners.at(markerIdx);
		for (int pointIdx=0; pointIdx<points.size(); pointIdx++) {
			Point2f point = points.at(pointIdx);
			
			printf("Markerpoint %d, %d, %d, %d\n", markerIdx, markerId, (int)point.x, (int)point.y);
	
		}

	}

	printf("Markerids: %d\n", markerIds.size());

	/*
	line( frame,
        	Point(0,0),
        	Point(10,10),
        	Scalar( 0, 0, 0 ),
        	5,
        	8 );
	*/

        imshow("edges", frame);
        if(waitKey(30) >= 0) break;
    }
    // the camera will be deinitialized automatically in VideoCapture destructor
    return 0;
}
