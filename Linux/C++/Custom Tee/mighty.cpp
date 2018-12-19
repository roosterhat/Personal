#include <sys/wait.h>
#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>
#include <string.h>
#include <fstream>

using namespace std;

int main(int argc, char ** args){
	int pipefd[2];
	pid_t pid;	
	if(argc!=3){
		printf("Invalid Syntax, expected usage: mighty <source> <destination>\n");
		return 1;
	}
	ifstream f(args[1]);
	if(!f.good()){
		printf("Invalid Argument: file '%s' does not exist\n",args[1]);
		return 1;
	}
	if(pipe(pipefd) == -1){
		perror("Failed to create pipe\n");
		return 1;
	}
	if((pid = fork()) == -1){
		perror("Failed to fork\n");
		return 1;
	}
	if(pid == 0){
		close(pipefd[0]);
		dup2(pipefd[1], STDOUT_FILENO);
		execl("/bin/cat","cat","-b",args[1],(char *)0);
		perror("Failed to call excel: cat\n");
		return 1;
	}
	else{
		FILE * out = fopen(args[2],"w");
		close(pipefd[1]);
		char buffer;
		int status,bufferlen;
		while((bufferlen = read(pipefd[0], &buffer, 1)) > 0 ||  waitpid(-1,&status,WNOHANG) != -1){
			if(bufferlen > 0){
				printf("%c",buffer);
				fwrite(&buffer, 1, 1, out);
			}
		}
		fclose(out);
		wait(NULL);
	}
	return 0;
}
