#include <gsl/gsl_rng.h>
#include <stdio.h>
#include <iostream>
#include <cstdlib>
#include <sys/time.h>
#include <math.h>
#include <cstring>

bool isInteger(char * c);

typedef unsigned long long timestamp_t;

static timestamp_t get_timestamp ()
{
      struct timeval now;
      gettimeofday (&now, NULL);
      return  now.tv_usec + (timestamp_t)now.tv_sec * 1000000;
}

int main(int argc, char ** args){
	gsl_rng * r;
	int size = 1000;
	if(argc==2){
		if(isInteger(args[1])){	
			size = atoi(args[1]);
			if(size < 1){
				printf("Invalid Arguments: expected integer greater than 0, found: %d\n",size);
				exit(1);
			}
		}
		else{
			printf("Invalid Arguments: expected integer, found: %s\n",args[1]);
			exit(1);
		}
	}

        r = gsl_rng_alloc(gsl_rng_mt19937);
        struct timeval tp;
        gettimeofday(&tp, NULL);
        gsl_rng_set(r,tp.tv_sec * 1000 + tp.tv_usec / 1000);
	
	int count = 0,in = 0;
	/*for(int i = 0; i < size; i++){
	        double x = gsl_rng_uniform(r);
		double y = gsl_rng_uniform(r);
		if(sqrt(x*x+y*y) <= 1)
			count++; 
	}*/
		
	long time = 1000000*100;
	for(timestamp_t t = get_timestamp(); get_timestamp() < t + time;){
		double x = gsl_rng_uniform(r);
		double y = gsl_rng_uniform(r);
		if(sqrt(x*x+y*y) <= 1)
			in++;
		count++;
	}



	printf("%d,%f\n",count, 4*(double)in/count);
        gsl_rng_free(r);
	return 0;
}

bool isInteger(char * c){
        for(int i = 0; i < strlen(c); i++)
                if(!isdigit(c[i]))
                        return false;
        return true;
}


