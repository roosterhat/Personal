#include <stdio.h>
#include <iostream>
#include <ctype.h>
#include <stdlib.h>
#include <unistd.h>
#include <string>
#include <cstring>
#include <cstdlib>
#include <sys/types.h>
#include <sys/wait.h>
#include "dealer.h"



using namespace std;

int main(int argc, char** args){
	int c;
	while ((c = getopt (argc, args, "hp:vt:")) != -1){
		switch (c){
			case 'h':
				printf( "Runs a specified amount of instances of a program designed to return true/false based on a percentage change and averages the results\n"
					"Usage: dealer <options> <arguments>\n\n"
					"Options:\n"
					"-h	Displays help information\n"
					"-p	Specifies the percentage chance of success [0-100]\n"
					"-v	Verbose\n"
					"-t	Specifies the number of concurrent child processes [1-8]\n");
				exit(0);
			case 'p':
				if (isInteger(optarg)){
					percentage = atoi(optarg);
                     			if(percentage>100 || percentage<0){
                        			printf("Invalid Argument: percentage, expected integer in range [0-100], found: %d\n",percentage);
                         			return 1;
                     			}
				 }
				else{
					printf("Invalid Argument: expected integer, found: %s\n",optarg);
					return 1;
				}
				break;
			case 'v':
				verbose = true;
				break;
			case 't':
				if (isInteger(optarg)){
					int num = atoi(optarg);
					if(num > 0 && num <= 8)
                                        	childProcesses = atoi(optarg);
					else{
						printf("Invalid Parameter: expected integer in range [1-8]");
						return 1;
					}
        			}	
        			else{
               				printf("Invalid Argument: expected integer, found: %s\n",optarg);
                			return 1;
        			}
				break;
			case '?':
				printf("Unknown Argument: %s\nTry -h for help",optarg);
				return 1;
			default:
				break;			
			}
	}
	if(optind<argc)
		if(isInteger(args[optind]))
			trials = atoi(args[optind]);
		
	runTrials(trials, percentage, childProcesses, verbose);
	return 0;
}

void runTrials(int trials,int percentage,  int childProc, bool verbose ){
    	pid_t pid;
	int numProc = 0, checked = 0;
	int failed = 0;
	char  num [4];
	int status;
	sprintf(num,"%d",percentage);
	pid_t procs [trials];
   	for(int iter = 0; iter<=trials ||  checked<trials;){
     		while(numProc<childProcesses && iter<=trials){
	            	pid_t pid = fork();
       		     	if(pid==0){        
       		         	execl("/home/011/j/je/jeo170030/homework/hw5_jeo170030/hand", "hand", num, "-s",(char*)0);
 		              	perror("Error calling excel");
				exit(1);	
            		}
            		else{
              			numProc++;
             			procs[iter++] = pid;
        		}
        	}
		int status;
		for(int i = 0; i < iter; i++){
                                        if(procs[i] != -1 && (pid = waitpid(procs[i], &status, WNOHANG)) > 0){
                                                if(WIFEXITED(status)){
                                                        int stat = WEXITSTATUS(status);
                                                        if(stat == 0 || stat == 1){
                                                                numProc--;
                                                                failed += (stat ? 0 : 1);
                                                                if(verbose)
                                                                        printf("%d PID %d returned: %s\n",i,procs[i], (stat ? "Success" : "Failure"));
                                                                procs[i] = -1;
								checked++;
                                                        }
                                                }
                                        }
                                }
	}
    	int fail = (double)failed/trials*100;
	int succ = 100 - fail;
	printf("Created %d proccesses\n", trials);
	printf("Success - %d%%\n",succ);
	printf("Failure - %d%%\n",fail);
}

bool isInteger(char * c){
	for(int i = 0; i < strlen(c); i++)
		if(!isdigit(c[i]))
			return false;
	return true;
}
