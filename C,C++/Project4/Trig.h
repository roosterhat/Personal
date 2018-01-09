/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/* 
 * File:   Trig.h
 * Author: erik ostlind
 * ID: jeo170030
 *
 * Created on November 3, 2017, 6:21 PM
 */

#include <string>
#include "Function.h"

using namespace std;

#ifndef TRIG_H
#define TRIG_H

class Node;

class Sin: public Function{
    int ceofficient;
    void derive(Node* l);
    string getValue();
    bool equal(string val);
};

class Cos: public Function{
public:
    int ceofficient;
    void derive(Node* l);
    string getValue();
    bool equal(string val);
};

class Tan: public Function{
public:
    int ceofficient;
    void derive(Node* l);
    string getValue();
    bool equal(string val);
};

class Sec: public Function{
public:
    int ceofficient;
    void derive(Node* l);
    string getValue();
    bool equal(string val);
};

class Csc: public Function{
public:
    int ceofficient;
    void derive(Node* l);
    string getValue();
    bool equal(string val);
};

class Cot: public Function{
public:
    int ceofficient;
    void derive(Node* l);
    string getValue();
    bool equal(string val);
};

#endif /* TRIG_H */

