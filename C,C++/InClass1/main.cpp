/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/* 
 * File:   main.cpp
 * Author: Erik Ostlind  jeo170030
 *         Graoson Ayres gga160030
 *
 * Created on September 14, 2017, 1:14 PM
 */

#include <cstdlib>
#include <iostream>
#include <string>
#include <stdlib.h>

using namespace std;

bool sequentialSearch(int* array,int size, int value);
void decToBi(int number);
void genArray(int* array, int size);

int main() {
    int size = 15;
    int* array = new int[size];
    genArray(array,size);
    while(true){
        string num;
        cout<<"Enter a number(0-"<<size*2-1<<"): ";
        getline(cin,num);
        int number = std::stoi(num);
        if(sequentialSearch(array,size,number)){
            decToBi(number);
            cout<<endl;
        }
        else
            cout<<"Your number doesnt exist"<<endl;
    }
    return 0;
}

void genArray(int* array, int size){
    for(int i=0;i<size;i++){
        while(true){
            int rando = rand()%(size*2);
            if(!sequentialSearch(array,i+1,rando)){
                array[i]=rando;
                break;
            }
        }
    }
}

bool sequentialSearch(int* array, int size, int value){
    if(size==0)
        return false;
    else
        size--;
    if(array[size]==value)
        return true;
    else
        return sequentialSearch(array,size,value);
}

void decToBi(int number){
    if(number==0)
        return;
    decToBi(number/2);   
    cout<<(number)%2;    
}
