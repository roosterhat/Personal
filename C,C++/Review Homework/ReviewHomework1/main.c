/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/* 
 * File:   main.c
 * Author: eriko
 *
 * Created on September 5, 2017, 1:07 PM
 */

#include <stdio.h>
#include <stdlib.h>

void buildArray(int* array, int n);
void displayArray(int* array, int n);
void bubbleSort(int array[], int n);
void selectionSort(int array[], int n);

int main() {
    int size = 8;
    int a1[size];
    int a2[size];
    buildArray(a1,size);
    buildArray(a2,size);
    bubbleSort(a1,size);
    selectionSort(a2,size);
    return (EXIT_SUCCESS);
}

void buildArray(int array[], int n)
{
    for(int i=0;i<n;i++)
        array[i] = rand()%1000;
}

void displayArray(int array[], int n)
{
    printf("{");
    for(int i=0;i<n;i++){
        printf("%d",array[i]);
        if(i<n-1)
            printf(",");
    }
    printf("}\n");  
}

void swap(int *xp, int *yp)
{
    int temp = *xp;
    *xp = *yp;
    *yp = temp;
}
 
void bubbleSort(int array[], int n)
{
    printf("Bubble Sort\n");
    displayArray(array,n);
    for (int i=0;i<n-1;i++){      
        for (int j=0;j<n-i-1;j++) 
            if (array[j] > array[j+1])
                swap(&array[j], &array[j+1]);
        displayArray(array,n);
    }
}

void selectionSort(int array[], int n)
{
    printf("Selection Sort\n");
    displayArray(array,n);
    int min;
    for (int i=0;i<n-1;i++)
    {
        min = i;
        for (int j=i+1;j<n;j++)
            if(array[j] < array[min])
                min = j;
        swap(&array[min], &array[i]);
        displayArray(array,n);
    }
}
