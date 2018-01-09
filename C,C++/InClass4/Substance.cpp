/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/* 
 * File:   Substance.cpp
 * Author: eriko
 * 
 * Created on November 7, 2017, 1:17 PM
 */

#include "Substance.h"

Substance::Substance(string name, int freeze, int boiling){
    this->name = name;
    this->freezingPoint = freeze;
    this->boilingPoint = boiling;
}

bool Substance::isBoiling(int temperature){
    return boilingPoint<=temperature;
}

bool Substance::isFreezing(int temperature){
    return freezingPoint>=temperature;
}

string Substance::toString(){
    return name+"|| Freezing Point: "+to_string(freezingPoint)+" Boiling Point: "+to_string(boilingPoint);
}