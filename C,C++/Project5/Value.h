/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/* 
 * File:   Value.h
 * Author: erik ostlind
 * jeo170030
 * Created on November 1, 2017, 12:54 PM
 */
#include<string>
using namespace std;

#ifndef VALUE_H
#define VALUE_H

class Value {
public:
    virtual string getValue()=0;
    virtual bool equal(string val)=0;
    bool equal(Value* val){return equal(val->getValue());};
    bool operator==(string val){return equal(val);}
    bool operator==(Value* val){return equal(val);}
    bool operator!=(string val){return !equal(val);}
    bool operator!=(Value* val){return !equal(val);}
};

#endif /* VALUE_H */

