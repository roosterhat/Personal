/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/* 
 * File:   Circle.h
 * Author: erik ostlind
 * jeo170030
 * Kevin Salinda
 * kms170630
 * Created on December 5, 2017, 1:14 PM
 */

#include "BasicShape.h"

#ifndef CIRCLE_H
#define CIRCLE_H

class Circle: public BasicShape {
public:
    Circle();
    Circle(double,double,double);
    void setX(double);
    void setY(double);
    void setRadius(double);
    double getX();
    double getY();
    double getRadius();
    void calculateArea();
    void calculatePerimeter();
protected:
    double x;
    double y;
    double radius;
};

#endif /* CIRCLE_H */

