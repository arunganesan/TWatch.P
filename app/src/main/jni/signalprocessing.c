#include "signalprocessing.h"
#define APPNAME "DSPNDK"



void do_xcorr (jdouble * output, jdouble * signal, jdouble * chirp, jint outputlen, jint signallen, jint chirplen) {
  int si, ci;
  double max = 0.0;
  for (si = 0; si < signallen - chirplen; si++) {
    double sum_xcorr = 0.0;
    for (ci = 0; ci < chirplen; ci++)  sum_xcorr += signal[si+ci]*chirp[ci];
    output[si] = sum_xcorr;
    if (sum_xcorr > max) max = sum_xcorr;
  }

  //__android_log_print(ANDROID_LOG_ERROR, APPNAME, "Maximum in xcorr is %f", max);;
}