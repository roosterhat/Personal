/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/* 
 * File:   main.cpp
 * Author: erik ostlind
 * jeo170030
 * Kevin Salinda
 * kms170630
 *
 * Created on December 5, 2017, 1:11 PM
 */
#include "BasicShape.h"
#include "Circle.h"
#include "Rectangle.h"
#include <cstdlib>
#include <iostream>

using namespace std;

/*
 * 
 */
void displayShape(BasicShape* shape);

int main() {
    displayShape(new Circle(0,0,3));
    displayShape(new Circle());
    displayShape(new Rectangle(2,3));
    displayShape(new Rectangle());
    return 0;
}

void displayShape(BasicShape* shape){
    cout<<shape->getName()<<" Area: "<<shape->getArea()<<" Perimeter: "<<shape->getPerimeter()<<endl;
}
