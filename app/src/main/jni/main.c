#include "edu_umich_eecs_twatchp_AutoTuner.h"

#include <stdio.h>
#include <stdlib.h>
#include <math.h>

#define APPNAME "NDKSIDE"

#include "generalmath.h"
#include "signalprocessing.h"

#define MINIMUM_SPACING 1102
#define WINDOW_START -25
#define WINDOW_END 200

  double get_threshold (jdouble * xcorr,  jint xcorr_length) {
    int i = argmax(xcorr, xcorr_length);
    return xcorr[i]*0.45;

  	//double stdev = get_stdev(xcorr, xcorr_length);
  	//printf("Threshold is %f\n", 4*stdev);
  	//return 4*stdev;
  }

  int find_peaks (jdouble * xcorr, int * peaks, double threshold, jint xcorr_length, int peaks_length) {
  	int i, end_point, max_x;
  	int first_peaks_counter = 0;
  	int first_pass_peaks [200];
  	for (i = 0; i < xcorr_length; i += MINIMUM_SPACING) {
  		end_point = MINIMUM_SPACING-1;
  		if (end_point+i > xcorr_length - 1)
  			end_point = (xcorr_length-1) - i;

  		max_x = i + argmax(xcorr+i, end_point);
  		if (xcorr[max_x] > threshold) {
  			first_pass_peaks[first_peaks_counter++] = max_x;
  		}
  	}

  	if (first_peaks_counter == 0) return 0;
  	//printf("Got %d max peaks so far.\n", first_peaks_counter);
  	int correct_peak_index = 0;

  	int prev_peak = first_pass_peaks[0], curr_peak;

  	for (i = 1; i < first_peaks_counter; i++) {
  		curr_peak = first_pass_peaks[i];
  		if (curr_peak - prev_peak > MINIMUM_SPACING) {
  			peaks[correct_peak_index++] = prev_peak;
  			prev_peak = curr_peak;
  		} else if (xcorr[prev_peak] < xcorr[curr_peak]) {
  			prev_peak = curr_peak;
  		}

  		if (correct_peak_index >= peaks_length) return -100;
  	}

  	peaks[correct_peak_index++] = prev_peak;
  	return correct_peak_index;
  }

  int find_windows (int * peaks, jint * windows, jint signallen, int num_peaks, jint windowlen) {
  	int widx = 0, peak, i, start, end;

  	for (i = 0; i < num_peaks; i++) {
  		peak = peaks[i];
  		start = peak + WINDOW_START;
  		//if (start < 0) start = 0;
  		if (start < 0) continue;
  		end = peak + WINDOW_END;
  		if (end > signallen-1) break;
  		//if (windowlen < widx+2) break;
  		windows[widx++] = start;
  		windows[widx++] = end;
  	}
  	return widx;
  }















 JNICALL jint Java_edu_umich_eecs_twatchp_AutoTuner_findchirps
  (JNIEnv * env, jobject obj, jdoubleArray signal, jdoubleArray chirp, jdoubleArray xcorr, jintArray peaks, jint signallen, jint chirplen, jint corrlength, jint peakslen) {
    //__android_log_print(6, "fiblib.c", "Double val: %f", chirp[1]);

    //printf("In and running captain.\n");
    //fflush(stdout);

    jboolean isCopy;
    jdouble *signalElts = (*env)->GetDoubleArrayElements(env, signal, &isCopy);
    jdouble *chirpElts = (*env)->GetDoubleArrayElements(env, chirp, &isCopy);
    jdouble *xcorrElts = (*env)->GetDoubleArrayElements(env, xcorr, &isCopy);
    jint * peaksElts = (*env)->GetIntArrayElements(env, peaks, &isCopy);

    // XXX: If any of these is in fact Copy, then we need to copy the value back
    // and make sure we release the local copy.

	double threshold;
  	int num_peaks;

  	do_xcorr(xcorrElts, signalElts, chirpElts, corrlength, signallen, chirplen);

  	threshold = get_threshold(xcorrElts, corrlength);

  	__android_log_print(ANDROID_LOG_ERROR, APPNAME, "Threshold is %f.", threshold);

  	num_peaks = find_peaks(xcorrElts, peaksElts, threshold, corrlength, peakslen);

    __android_log_print(ANDROID_LOG_ERROR, APPNAME, "Found %d peaks before windowing.", num_peaks);


    (*env)->ReleaseDoubleArrayElements(env, signal, signalElts, 0);
    (*env)->ReleaseDoubleArrayElements(env, chirp, chirpElts, 0);
    (*env)->ReleaseDoubleArrayElements(env, xcorr, xcorrElts, 0);
    (*env)->ReleaseIntArrayElements(env, peaks, peaksElts, 0);

    return num_peaks;
  }




