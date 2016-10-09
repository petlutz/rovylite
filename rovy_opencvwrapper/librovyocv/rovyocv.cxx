#include <opencv2/highgui.hpp>
#include <opencv2/opencv.hpp>
#include <opencv2/aruco.hpp>

#include "de_gnox_rovy_ocv_RovyOpenCVWrapper.h"

using namespace std;
using namespace cv;

VideoCapture cap(0);

int cam = 0;
bool frameRetrieved = false;
Mat frame;

int arucoMarkerDict = 2;
bool arucoMarkerDetected = false;
vector< int > arucoMarkerIds; 
vector< vector<Point2f> > arucoMarkerCorners;


JNIEXPORT jint JNICALL Java_de_gnox_rovy_ocv_RovyOpenCVWrapper_nOpenVideoCapture
  (JNIEnv *, jobject, jint jcam, jboolean initWindow) 
{
	cam = jcam;
	if (initWindow)
    	namedWindow("opencv",WINDOW_NORMAL);
	return cap.open(cam);
}

JNIEXPORT jint JNICALL Java_de_gnox_rovy_ocv_RovyOpenCVWrapper_nGrab
  (JNIEnv *, jobject) 
{
	frameRetrieved = false;
	return cap.grab();
}


bool retrieveFrameIfNeeded() {
	if (frameRetrieved) 
		return true;
	if (!cap.retrieve(frame))
		return false;
	frameRetrieved = true;
	return true;
}


JNIEXPORT jint JNICALL Java_de_gnox_rovy_ocv_RovyOpenCVWrapper_nReleaseVideoCapture
  (JNIEnv *, jobject)
{
	cap.release();	
	return 1;
}

JNIEXPORT jint JNICALL Java_de_gnox_rovy_ocv_RovyOpenCVWrapper_nArucoDetectMarkers
  (JNIEnv *env, jobject, jint markerDict) 
{
	if (!retrieveFrameIfNeeded())
		return -1;

    vector< vector<Point2f> > rejectedCandidates; 

    Ptr<aruco::DetectorParameters> parameters = aruco::DetectorParameters::create(); 
    parameters->doCornerRefinement = true;
    Ptr<aruco::Dictionary> dictionary = aruco::getPredefinedDictionary(aruco::PREDEFINED_DICTIONARY_NAME(markerDict));

    aruco::detectMarkers(frame, dictionary, arucoMarkerCorners, arucoMarkerIds, parameters, rejectedCandidates);
    arucoMarkerDetected = true;

	return arucoMarkerIds.size();

/*
	int resultArrayMaxLength = arucoMarkerIds.size() * 16;
	jint resultArray[resultArrayMaxLength];
	for (int i = 0; i < resultArrayMaxLength; i++)
		resultArray[i]=-1;

	int idxResultArray = 0;
	for (int markerIdx=0; markerIdx<arucoMarkerIds.size(); markerIdx++) {
		int markerId = arucoMarkerIds.at(markerIdx);
		vector<Point2f> points = arucoMarkerCorners.at(markerIdx);
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
*/
	
}

JNIEXPORT jintArray JNICALL Java_de_gnox_rovy_ocv_RovyOpenCVWrapper_nArucoGetMarkerCorners
  (JNIEnv *env, jobject, jint markerIdx)
{
	if (!arucoMarkerDetected)
		return env->NewIntArray(0);;

	vector<Point2f> points = arucoMarkerCorners.at(markerIdx);
	int resultArrayLength = points.size() * 2;
	jint resultArray[resultArrayLength];
	int idxResultArray = 0;
	for (int pointIdx=0; pointIdx<points.size(); pointIdx++) {
		Point2f point = points.at(pointIdx);			
		resultArray[idxResultArray++] = (int)point.x;
		resultArray[idxResultArray++] = (int)point.y;
	}
	
   jintArray resultArrayJNI = env->NewIntArray(resultArrayLength);  // allocate
   if (NULL == resultArrayJNI) return NULL;
   env->SetIntArrayRegion(resultArrayJNI, 0 , resultArrayLength, resultArray);  // copy
   return resultArrayJNI;
}

JNIEXPORT jint JNICALL Java_de_gnox_rovy_ocv_RovyOpenCVWrapper_nArucoGetMarkerId
  (JNIEnv *, jobject, jint markerIdx)
{
	if (!arucoMarkerDetected)
		return -1;
	return arucoMarkerIds.at(markerIdx);
}

JNIEXPORT jint JNICALL Java_de_gnox_rovy_ocv_RovyOpenCVWrapper_nArucoDrawDetectedMarkers
  (JNIEnv *, jobject)
{

	if (!arucoMarkerDetected)
		return -1;
	if (arucoMarkerIds.size() > 0)
		aruco::drawDetectedMarkers(frame, arucoMarkerCorners, arucoMarkerIds);
	return 0;
}


JNIEXPORT jint JNICALL Java_de_gnox_rovy_ocv_RovyOpenCVWrapper_nImshow
  (JNIEnv *, jobject)
{
	if (!retrieveFrameIfNeeded())
		return -1;
	imshow("opencv", frame);
   	waitKey(1);  
}


