#include <opencv2/highgui.hpp>
#include <opencv2/opencv.hpp>
#include <opencv2/aruco.hpp>

#include "de_gnox_rovy_ocv_RovyOpenCVWrapper.h"

using namespace std;
using namespace cv;

bool arucoEstimatePose = false;
float arucoMarkerLength = 0.0;
bool arucoMarkerDetected = false;
aruco::DetectorParameters arucoParameters;
aruco::Dictionary arucoDictionary;
vector< int > arucoMarkerIds; 
vector< vector<Point2f> > arucoMarkerCorners;
vector< Vec3d > arucoMarkerRotationVectors, arucoMarkerTranslationVectors;
Mat camMatrix, distCoeffs;

bool readCameraParameters(string filename, Mat &camMatrix, Mat &distCoeffs) {
    FileStorage fs(filename, FileStorage::READ);
    if(!fs.isOpened())
        return false;
    fs["camera_matrix"] >> camMatrix;
    fs["distortion_coefficients"] >> distCoeffs;
    return true;
}

JNIEXPORT jint JNICALL Java_de_gnox_rovy_ocv_RovyOpenCVWrapper_nArucoInitWithPoseEstimation
  (JNIEnv *, jobject, jint markerDict, jfloat markerLength)
{
	arucoParameters.doCornerRefinement = true;
	arucoDictionary = aruco::getPredefinedDictionary(aruco::PREDEFINED_DICTIONARY_NAME(markerDict));

	bool readOk = readCameraParameters("camparam.json", camMatrix, distCoeffs);
	if(!readOk) {
	cerr << "Invalid camera file" << endl;
	return -1;
	}

	arucoMarkerLength = markerLength;
	arucoEstimatePose = true;

	return 0;
}

JNIEXPORT jint JNICALL Java_de_gnox_rovy_ocv_RovyOpenCVWrapper_nArucoInit
  (JNIEnv *, jobject, jint markerDict)
{
	arucoParameters.doCornerRefinement = true;
	arucoDictionary = aruco::getPredefinedDictionary(aruco::PREDEFINED_DICTIONARY_NAME(markerDict));

	arucoMarkerLength = 0;
	arucoEstimatePose = false;
	return 0;
}

JNIEXPORT jint JNICALL Java_de_gnox_rovy_ocv_RovyOpenCVWrapper_nArucoDetectMarkers
  (JNIEnv *env, jobject, jlong frameAddr) 
{
	Mat* frame = (Mat*) frameAddr;

	vector< vector<Point2f> > rejectedCandidates; 
	aruco::detectMarkers(*frame, arucoDictionary, arucoMarkerCorners, arucoMarkerIds, arucoParameters, rejectedCandidates);

	if(arucoEstimatePose)
	aruco::estimatePoseSingleMarkers(arucoMarkerCorners, arucoMarkerLength, camMatrix, distCoeffs, arucoMarkerRotationVectors, arucoMarkerTranslationVectors);


	arucoMarkerDetected = true;
	return arucoMarkerIds.size();	
}

JNIEXPORT jintArray JNICALL Java_de_gnox_rovy_ocv_RovyOpenCVWrapper_nArucoGetMarkerCorners
  (JNIEnv *env, jobject, jint markerIdx)
{
	if (!arucoMarkerDetected)
		return env->NewIntArray(0);

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

JNIEXPORT jdoubleArray JNICALL Java_de_gnox_rovy_ocv_RovyOpenCVWrapper_nArucoGetMarkerRotationMatrix
  (JNIEnv *env, jobject, jint markerIdx)
{
	if (!arucoMarkerDetected || !arucoEstimatePose)
		return NULL;

	Vec3d vec = arucoMarkerRotationVectors.at(markerIdx);

	double rotMatrix[9] = {1,0,0,
                               0,-1,0,
                               0,0,-1};
	Mat rotMat(3,3,CV_64FC1, rotMatrix);
	Rodrigues(vec, rotMat);	
	int resultArrayLength = 9;


	jdoubleArray resultArrayJNI = env->NewDoubleArray(resultArrayLength);  // allocate
	if (NULL == resultArrayJNI) return NULL;
	env->SetDoubleArrayRegion(resultArrayJNI, 0 , resultArrayLength, rotMatrix);  // copy
	return resultArrayJNI;
}

JNIEXPORT jdoubleArray JNICALL Java_de_gnox_rovy_ocv_RovyOpenCVWrapper_nArucoGetMarkerTranslationVector
  (JNIEnv *env , jobject, jint markerIdx)
{
	if (!arucoMarkerDetected || !arucoEstimatePose)
		return NULL;

	int resultArrayLength = 3;
	Vec3d vec = arucoMarkerTranslationVectors.at(markerIdx);
	jdoubleArray resultArrayJNI = env->NewDoubleArray(resultArrayLength);  // allocate
	if (NULL == resultArrayJNI) return NULL;
	env->SetDoubleArrayRegion(resultArrayJNI, 0 , resultArrayLength, vec.val);  // copy
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
  (JNIEnv *, jobject, jlong frameAddr)
{

	Mat* frame = (Mat*) frameAddr;

	if (!arucoMarkerDetected)
		return -1;
	if (arucoMarkerIds.size() > 0) {
		aruco::drawDetectedMarkers(*frame, arucoMarkerCorners, arucoMarkerIds);
		if (arucoEstimatePose) 
			for(unsigned int i = 0; i < arucoMarkerIds.size(); i++)
            	aruco::drawAxis(*frame, camMatrix, distCoeffs, arucoMarkerRotationVectors[i], arucoMarkerTranslationVectors[i], arucoMarkerLength * 0.5f);
	}		
	
	return 0;
}

