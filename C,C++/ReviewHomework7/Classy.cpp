/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/* 
 * File:   Classy.cpp
 * Author: eriko
 * 
 * Created on November 7, 2017, 10:02 AM
 */
#include <iostream>
#include "Classy.h"
#include <string>

using namespace std;

Classy::Classy(){
   values = new float[10]; 
   size = 10;
}
Classy::Classy(int n){
    n = n>0 ? n : 10;
    values = new float[n];
    size = n;
}
void Classy::set(float value, int index){
    if(index>=0 && index<size)
        values[index] = value;
}
float Classy::get(int index){
    if(index>=0 && index<size)
        return values[index];
    return -1.0;
}
float Classy::highest(){
    float highest = values[0];
    for(int i=0;i<size;i++){
        float f = values[i];
        if(f>highest)
            highest = f;
    }
    return highest;
}
float Classy::lowest(){
    float lowest = values[0];
    for(int i=0;i<size;i++){
        float f = values[i];
        if(f<lowest)
            lowest = f;
    }
    return lowest;
}
float Classy::average(){
    float total = 0.0;
    for(int i=0;i<size;i++)
        total+=values[i];
    return total/size;
}
int Classy::getSize(){
    return size;
}
string Classy::toString(){
    string res = "";
    for(int i=0;i<size;i++){
        string val = to_string(values[i]);
        res+= val.substr(0,val.find(".")+3)+",";
    }
    return res.substr(0,res.size()-1);
}

