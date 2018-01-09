/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/* 
 * File:   Classy.h
 * Author: eriko
 *
 * Created on November 7, 2017, 10:02 AM
 */

#include <string>

using namespace std;

#ifndef CLASSY_H
#define CLASSY_H

class Classy {
public:
    float* values;
    Classy();
    Classy(int n);
    void set(float value, int index);
    float get(int index);
    float highest();
    float lowest();
    float average();
    int getSize();
    string toString();
private:
    int size;
};

#endif /* CLASSY_H */

