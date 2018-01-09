/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/* 
 * File:   Node.h
 * Author: erik ostlind
 * ID: jeo170030
 *
 * Created on November 1, 2017, 12:56 PM
 */
#include <string>
#include "Value.h"


#ifndef NODE_H
#define NODE_H

#include "Trig.h"

using namespace std;

class Node:public Value {
public:
    double coefficient;
    double exponent;
    double functionCoefficient;
    Function* function;
    Node(double c, double e, double fc, Function* t = nullptr);
    Node(int c, int e, int fc, Function* t = nullptr);
    string getValue();
    bool equal(string val); 
    void derive();
private:
    string formatFloat(double f,int n=4);
    string realToFraction(double value, double accuracy=1e-10);
};

#endif /* NODE_H */

