#include <stdio.h>
#include <sys/types.h>
#include <signal.h>
#include <unistd.h>
#include <sys/time.h>
#include <cstdlib>
#include <gsl/gsl_rng.h>

void childCatch(int signal);
void parentCatch(int signal);

static pid_t pid;

int main(){
	pid = fork();
	if(pid == 0){
		for(int i = 1; i<31; i++)
			signal(i,childCatch);
		for(;;){
			printf("Child waiting\n");
			usleep(500000);
		}
	}
	else{
		for(int i = 1; i<=31; i++)
			signal(i,parentCatch);
		gsl_rng * r = gsl_rng_alloc(gsl_rng_mt19937);
		struct timeval tp;
		gettimeofday(&tp, NULL);
		gsl_rng_set(r,tp.tv_sec * 1000 + tp.tv_usec / 1000);
		for(;;)	{
			int sig = (int)(gsl_rng_uniform(r) * 30 + 1);
			printf("Parent sending signal %d\n",sig);
			kill(pid, sig);
			usleep(1000000);
		}
		gsl_rng_free(r);
	}
	return 0; 
}

void parentCatch(int signal){
	printf("Parent received signal %d\n", signal);
	kill(pid, SIGKILL);
	wait(pid);
	printf("Parent exiting\n");
	exit(0);
}

void childCatch(int signal){
	printf("Child received signal %d\n",signal);
}
