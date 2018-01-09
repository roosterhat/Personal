/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/* 
 * File:   Substance.h
 * Author: eriko
 *
 * Created on November 7, 2017, 1:17 PM
 */

#include <string>

using namespace std;

#ifndef SUBSTANCE_H
#define SUBSTANCE_H

class Substance {
public:
    string name;
    int freezingPoint;
    int boilingPoint;
    Substance(string name, int freeze, int boiling);
    bool isFreezing(int temperature);
    bool isBoiling(int temperature);
    string toString();
};

#endif /* SUBSTANCE_H */

