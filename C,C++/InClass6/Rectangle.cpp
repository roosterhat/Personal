/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/* 
 * File:   Rectangle.cpp
 * Author: erik ostlind
 * jeo170030
 * Kevin Salinda
 * kms170630
 * Created on December 5, 2017, 1:25 PM
 */

#include "Rectangle.h"
#include "BasicShape.h"

Rectangle::Rectangle(): Rectangle(0.0,0.0){
}

Rectangle::Rectangle(double width, double height){
    name = "Rectangle";
    this->width = width;
    this->height = height;
    calculateArea();
    calculatePerimeter();
}

void Rectangle::setHeight(double height){
    this->height = height;
}

void Rectangle::setWidth(double width){
    this->width = width;
}

double Rectangle::getHeight(){
    return height;
}

double Rectangle::getWidth(){
    return width;
}

void Rectangle::calculateArea(){
    area = width*height;
}

void Rectangle::calculatePerimeter(){
    perimeter = 2*width+ 2*height;
}