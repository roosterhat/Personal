/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/* 
 * File:   Circle.cpp
 * Author: erik ostlind
 * jeo170030
 * Kevin Salinda
 * kms170630
 * Created on December 5, 2017, 1:14 PM
 */

#include "Circle.h"

#include <math.h>

using namespace std;

Circle::Circle(): Circle(0.0,0.0,0.0){
}

Circle::Circle(double x, double y, double radius){
    name = "Circle";
    this->x = x;
    this->y = y;
    this->radius = radius;
    calculateArea();
    calculatePerimeter();
}

void Circle::setX(double x){
    this->x = x;
}

void Circle::setY(double y){
    this->y = y;
}

void Circle::setRadius(double radius){
    this->radius = radius;
}

double Circle::getX(){
    return x;
}

double Circle::getY(){
    return y;
}

double Circle::getRadius(){
    return radius;
}

void Circle::calculateArea(){
    area = M_PI * pow(radius,2.0);
}

void Circle::calculatePerimeter(){
    perimeter = M_PI * 2 * radius;
}