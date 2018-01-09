/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/* 
 * File:   NumDays.cpp
 * Author: erik ostlind, seth bedford
 *jeo170030
 *sab170730
 * Created on November 16, 2017, 1:11 PM
 */

#include <iomanip>
#include <string>
#include <regex>
#include "NumDays.h"

NumDays::NumDays(){
    hours = 0;
    days = 0;
}

NumDays::NumDays(double hours){
    this->hours = hours>=0 ? hours : 0.0f;
    calculateDays();
}

NumDays::NumDays(NumDays& n){
    this->hours = n.getHours();
    this->days = n.getDays();
}

void NumDays::setHours(double hours){
    if(hours>=0)
        this->hours = hours;
    calculateDays();
}

void NumDays::setDays(double days){
    if(days>=0)
        this->days = days;
    calculateHours();
}

double NumDays::getHours(){
    return hours;
}

double NumDays::getDays(){
    return days;
}

void NumDays::calculateDays(){
    days = hours/8.0f;
}

void NumDays::calculateHours(){
    hours = days*8.0f;
}

double NumDays::operator +(NumDays& n){
    return hours+n.getHours();
}

double NumDays::operator -(NumDays& n){
    return hours-n.getHours();
}

bool NumDays::operator >(NumDays& n){
    return hours>n.getHours();
}

bool NumDays::operator <(NumDays& n){
    return hours<n.getHours();
}

NumDays NumDays::operator ++(){
    ++hours;
    calculateDays();
    return *this;
}

NumDays NumDays::operator ++(int){
    NumDays temp(hours++);
    calculateDays();
    return temp;
}

string NumDays::toString(){
    string h = to_string(hours);
    string d = to_string(days);
    h = regex_replace(h.substr(0,h.find(".")+3),regex("0{1,"+to_string(1)+"}$"),"");
    d = regex_replace(d.substr(0,d.find(".")+5),regex("0{1,"+to_string(3)+"}$"),"");
    return "Hours: "+h+" Days: "+d;
}