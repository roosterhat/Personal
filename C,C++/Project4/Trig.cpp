/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/* 
 * File:   Trig.cpp
 * Author: erik ostlind
 * ID: jeo170030
 * 
 * Created on November 3, 2017, 6:21 PM
 */

#include "Trig.h"
#include "Node.h"


string Sin::getValue(){
    return "sin ";
}

bool Sin::equal(string val){
    return getValue()==val;
}

void Sin::derive(Node* n){
    n->function = new Cos();
}

string Cos::getValue(){
    return "cos ";
}

bool Cos::equal(string val){
    return getValue()==val;
}

void Cos::derive(Node* n){
    n->functionCoefficient *= -1;
    n->function = new Sin();
}

string Tan::getValue(){
    return "tan ";
}

bool Tan::equal(string val){
    return getValue()==val;
}

void Tan::derive(Node* n){
    n->exponent *= 2;
    n->function = new Sec();
}

string Sec::getValue(){
    return "sec ";
}

bool Sec::equal(string val){
    return getValue()==val;
}

void Sec::derive(Node* n){
    n->function = new Sec();
}

string Csc::getValue(){
    return "csc ";
}

bool Csc::equal(string val){
    return getValue()==val;
}

void Csc::derive(Node* n){
    n->function = new Csc();
}

string Cot::getValue(){
    return "cot ";
}

bool Cot::equal(string val){
    return getValue()==val;
}

void Cot::derive(Node* n){
    n->function = new Cot();
}