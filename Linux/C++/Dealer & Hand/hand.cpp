#include <stdio.h>
#include <iostream>
#include <gsl/gsl_rng.h>
#include <cstring>
#include <cstdlib>
#include <sys/time.h>
#include <unistd.h>

using namespace std;
bool isInteger(char * c);

int main(int argc, char** args){
	gsl_rng * r;
	int percentage = 50;
	int c;
	bool silence = false;
	while((c = getopt(argc, args , "s")) != -1){
		switch (c){
			case 's':
				silence = true;
				break;
			case '?':
				printf("Unknown command\n");		
		} 
	}
	if (optind < argc){
		if(isInteger(args[optind])){
			percentage = atoi(args[optind]);
			if(!silence && (percentage > 100 || percentage < 0)){
				printf("Invalid Argument: expected integer in range [0-100], found:, %d\n",percentage);
				exit(1);
			}
		}
		else if(!silence){
			printf("Invaid Argument: expected integer found: %s\n",args[optind]);
			exit(1);
		}
	}
	

  	r = gsl_rng_alloc(gsl_rng_mt19937);	
	struct timeval tp;
	gettimeofday(&tp, NULL);
	gsl_rng_set(r,tp.tv_sec * 1000 + tp.tv_usec / 1000);
	double num = gsl_rng_uniform(r);
	
	gsl_rng_free(r);
	bool res = num * 100 < percentage;
	if(!silence)
		printf(res ? "success\n" : "failed\n");
	return res;
}

bool isInteger(char * c){
	for(int i = 0; i < strlen(c); i++)
		if(!isdigit(c[i]) || c[i]=='-' || c[i]=='+')
			return false;
	return true;
}
