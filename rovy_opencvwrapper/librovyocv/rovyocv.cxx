#include <opencv2/highgui.hpp>
#include <opencv2/opencv.hpp>
#include <opencv2/aruco.hpp>

#include "de_gnox_rovy_ocv_MarkerDetector.h"

using namespace std;
using namespace cv;

VideoCapture cap(0);
bool debug = false;
int markerDict = 2;
int cam = 0;

JNIEXPORT jint JNICALL Java_de_gnox_rovy_ocv_MarkerDetector_nInit
  (JNIEnv *, jobject, jboolean jdebug, jint jcam, jint jmarkerDict) {

	debug = jdebug;
	cam = jcam;
	markerDict = jmarkerDict;
	
	if (debug)
    	namedWindow("debug",WINDOW_NORMAL);

	return cap.open(cam);
}



JNIEXPORT jintArray JNICALL Java_de_gnox_rovy_ocv_MarkerDetector_nDetectMarkers
  (JNIEnv *env, jobject)
{
    if(!cap.isOpened()) 
        cap.open(cam);
	
    cap.grab();
    Mat frame;
    cap.retrieve(frame);
  
    vector< int > markerIds; 
    vector< vector<Point2f> > markerCorners, rejectedCandidates; 

    Ptr<aruco::DetectorParameters> parameters = aruco::DetectorParameters::create(); 
    parameters->doCornerRefinement = true;
    Ptr<aruco::Dictionary> dictionary = aruco::getPredefinedDictionary(aruco::PREDEFINED_DICTIONARY_NAME(markerDict));
    aruco::detectMarkers(frame, dictionary, markerCorners, markerIds, parameters, rejectedCandidates);
    
	if (debug) {
		if (markerIds.size() > 0)
			aruco::drawDetectedMarkers(frame, markerCorners, markerIds);
		imshow("debug", frame);
   		waitKey(1);   
	}

	int resultArrayMaxLength = markerIds.size() * 16;
	jint resultArray[resultArrayMaxLength];
	for (int i = 0; i < resultArrayMaxLength; i++)
		resultArray[i]=-1;

	int idxResultArray = 0;
	for (int markerIdx=0; markerIdx<markerIds.size(); markerIdx++) {
		int markerId = markerIds.at(markerIdx);
		vector<Point2f> points = markerCorners.at(markerIdx);
		for (int pointIdx=0; pointIdx<points.size(); pointIdx++) {
			Point2f point = points.at(pointIdx);			
			resultArray[idxResultArray++] = markerIdx;
			resultArray[idxResultArray++] = markerId;
			resultArray[idxResultArray++] = (int)point.x;
			resultArray[idxResultArray++] = (int)point.y;
		}

	}
   
   jintArray resultArrayJNI = env->NewIntArray(resultArrayMaxLength);  // allocate
   if (NULL == resultArrayJNI) return NULL;
   env->SetIntArrayRegion(resultArrayJNI, 0 , resultArrayMaxLength, resultArray);  // copy
   return resultArrayJNI;
}

JNIEXPORT jint JNICALL Java_de_gnox_rovy_ocv_MarkerDetector_nReleaseCamera
  (JNIEnv *, jobject) {

	cap.release();	
	return 1;
}
