/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/* 
 * File:   main.c
 * Author: erik ostlind 
 * jeo170030
 *
 * Created on September 28, 2017, 12:50 PM
 */

#include <stdio.h>
#include <stdlib.h>
#include <algorithm>

/*
 * 
 */
void bubble(char* array);
char getLetter(int score);

int main(int argc, char** argv) {
    char test[10] = {'o','p','x','e','t','g','d','c','b','a'};
    bubble(test);
    for (int i=0; i < 10; i++)
        printf("%c", test[i]);
    printf("\n");
    return (EXIT_SUCCESS);
}

void bubble(char* array){
    for (int i = 0; i < 10-1; i++){      
        for (int j = 0; j < 10-i-1; j++){ 
            if (*(array+j) > *(array+j+1)){
                char temp = *(array+j);
                *(array+j) = *(array+j+1);
                *(array+j+1) = temp;
            }
        }
    }               
}


