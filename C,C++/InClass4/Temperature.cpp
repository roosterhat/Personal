/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/* 
 * File:   Temperature.cpp
 * Author: eriko
 * 
 * Created on November 7, 2017, 1:15 PM
 */

#include "Temperature.h"
#include "Substance.h"

Temperature::Temperature(){}

Temperature::Temperature(int temperature){
    this->temperature = temperature;
}

void Temperature::setTemperature(int temperature){
    this->temperature = temperature;
}

int Temperature::getTemperature(){
    return temperature;
}

bool Temperature::isEthylBoiling(){
    return ethyl->isBoiling(temperature);
}
bool Temperature::isEthylFreezing(){
    return ethyl->isFreezing(temperature);
}
bool Temperature::isOxygenBoiling(){
    return oxygen->isBoiling(temperature);
}
bool Temperature::isOxygenFreezing(){
    return oxygen->isFreezing(temperature);
}
bool Temperature::isWaterBoiling(){
    return water->isBoiling(temperature);
}
bool Temperature::isWaterFreezing(){
    return water->isFreezing(temperature);
}

vector<Substance*> Temperature::getFreezing(int temperature){
    vector<Substance*> v;
    for(Substance* s: getSubstances())
        if(s->isFreezing(temperature))
            v.push_back(s);
    return v;
}

vector<Substance*> Temperature::getBoiling(int temperature){
    vector<Substance*> v;
    for(Substance* s: getSubstances())
        if(s->isBoiling(temperature))
            v.push_back(s);
    return v;
}

vector<Substance*> Temperature::getSubstances(){
    vector<Substance*> s = {ethyl,oxygen,water};
    return s;
}




