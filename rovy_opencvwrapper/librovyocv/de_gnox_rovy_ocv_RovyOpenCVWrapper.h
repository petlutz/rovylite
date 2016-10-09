/* DO NOT EDIT THIS FILE - it is machine generated */
#include <jni.h>
/* Header for class de_gnox_rovy_ocv_RovyOpenCVWrapper */

#ifndef _Included_de_gnox_rovy_ocv_RovyOpenCVWrapper
#define _Included_de_gnox_rovy_ocv_RovyOpenCVWrapper
#ifdef __cplusplus
extern "C" {
#endif
/*
 * Class:     de_gnox_rovy_ocv_RovyOpenCVWrapper
 * Method:    nArucoDetectMarkers
 * Signature: (I)I
 */
JNIEXPORT jint JNICALL Java_de_gnox_rovy_ocv_RovyOpenCVWrapper_nArucoDetectMarkers
  (JNIEnv *, jobject, jint);

/*
 * Class:     de_gnox_rovy_ocv_RovyOpenCVWrapper
 * Method:    nArucoGetMarkerCorners
 * Signature: (I)[I
 */
JNIEXPORT jintArray JNICALL Java_de_gnox_rovy_ocv_RovyOpenCVWrapper_nArucoGetMarkerCorners
  (JNIEnv *, jobject, jint);

/*
 * Class:     de_gnox_rovy_ocv_RovyOpenCVWrapper
 * Method:    nArucoGetMarkerId
 * Signature: (I)I
 */
JNIEXPORT jint JNICALL Java_de_gnox_rovy_ocv_RovyOpenCVWrapper_nArucoGetMarkerId
  (JNIEnv *, jobject, jint);

/*
 * Class:     de_gnox_rovy_ocv_RovyOpenCVWrapper
 * Method:    nArucoDrawDetectedMarkers
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_de_gnox_rovy_ocv_RovyOpenCVWrapper_nArucoDrawDetectedMarkers
  (JNIEnv *, jobject);

/*
 * Class:     de_gnox_rovy_ocv_RovyOpenCVWrapper
 * Method:    nOpenVideoCapture
 * Signature: (IZ)I
 */
JNIEXPORT jint JNICALL Java_de_gnox_rovy_ocv_RovyOpenCVWrapper_nOpenVideoCapture
  (JNIEnv *, jobject, jint, jboolean);

/*
 * Class:     de_gnox_rovy_ocv_RovyOpenCVWrapper
 * Method:    nGrab
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_de_gnox_rovy_ocv_RovyOpenCVWrapper_nGrab
  (JNIEnv *, jobject);

/*
 * Class:     de_gnox_rovy_ocv_RovyOpenCVWrapper
 * Method:    nImshow
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_de_gnox_rovy_ocv_RovyOpenCVWrapper_nImshow
  (JNIEnv *, jobject);

/*
 * Class:     de_gnox_rovy_ocv_RovyOpenCVWrapper
 * Method:    nReleaseVideoCapture
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_de_gnox_rovy_ocv_RovyOpenCVWrapper_nReleaseVideoCapture
  (JNIEnv *, jobject);

#ifdef __cplusplus
}
#endif
#endif
