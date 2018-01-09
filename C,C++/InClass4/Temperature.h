/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/* 
 * File:   Temperature.h
 * Author: eriko
 *
 * Created on November 7, 2017, 1:15 PM
 */
#include <string>
#include <vector>
#include "Substance.h"


using namespace std;

#ifndef TEMPERATURE_H
#define TEMPERATURE_H

class Temperature {
public:
    int temperature = 0;
    Substance* oxygen = new Substance("Oxygen",-362, -306);
    Substance* ethyl = new Substance("Ethyl Alcohol",-173, 172);
    Substance* water = new Substance("Water",32, 212);
    
    Temperature();
    Temperature(int temperature);
    
    void setTemperature(int temperature);
    int getTemperature();
    bool isEthylFreezing();
    bool isEthylBoiling();
    bool isOxygenFreezing();
    bool isOxygenBoiling();
    bool isWaterFreezing();
    bool isWaterBoiling();
    
    vector<Substance*> getBoiling(int temperature);
    vector<Substance*> getFreezing(int temperature);
private:
    vector<Substance*> getSubstances();
};

#endif /* TEMPERATURE_H */

