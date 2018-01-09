/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/* 
 * File:   BasicShape.h
 * Author: erik ostlind
 * jeo170030
 * Kevin Salinda
 * kms170630
 * Created on December 5, 2017, 1:15 PM
 */

#include <string>

using namespace std;

#ifndef BASICSHAPE_H
#define BASICSHAPE_H

class BasicShape {
public:
    double getArea();
    virtual void calculateArea() = 0;
    double getPerimeter();
    virtual void calculatePerimeter() = 0;
    string getName();
protected:
    string name;
    double area = 0;
    double perimeter = 0;
};

#endif /* BASICSHAPE_H */

