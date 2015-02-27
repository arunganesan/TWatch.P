#include <jni.h>
#include <android/log.h>


void do_xcorr (jdouble * output, jdouble * signal, jdouble * chirp, jint outputlen, jint signallen, jint chirplen);