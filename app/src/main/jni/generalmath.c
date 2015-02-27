#include "generalmath.h"

int argmax (double * array, int arraylen) {
	int i = 1, max_x = 0, max_val = array[0];
	for (i = 1; i < arraylen; i++)
		if (array[i] > array[max_x])
			max_x = i;
	return max_x;
}

double get_mean (double * xcorr, int xcorr_length) {
	double total = 0; int i;
	for (i = 0; i < xcorr_length; i++) total += xcorr[i];
	return total/xcorr_length;
}

double get_stdev (double * array, int arraylen) {
	double stdev, mean = get_mean (array, arraylen);
	double sosd = 0; int i;
	for (i = 0; i < arraylen; i++) sosd += pow(array[i]-mean, 2.0);
	stdev = sqrt(sosd/arraylen);
	return stdev;
}
