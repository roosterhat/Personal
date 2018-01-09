/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/* 
 * File:   Rectangle.h
 * Author: erik ostlind
 * jeo170030
 * Kevin Salinda
 * kms170630
 * Created on December 5, 2017, 1:25 PM
 */

#include "BasicShape.h"

#ifndef RECTANGLE_H
#define RECTANGLE_H

class Rectangle: public BasicShape {
public:
    Rectangle();
    Rectangle(double,double);
    void setWidth(double);
    void setHeight(double);
    double getWidth();
    double getHeight();
    void calculateArea();
    void calculatePerimeter();
protected:
    double width;
    double height;
};

#endif /* RECTANGLE_H */

