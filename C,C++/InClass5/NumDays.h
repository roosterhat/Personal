/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/* 
 * File:   NumDays.h
 * Author: erik ostlind, seth bedford
 *jeo170030
 *sab170730
 * Created on November 16, 2017, 1:11 PM
 */

#include <string>

using namespace std;

#ifndef NUMDAYS_H
#define NUMDAYS_H

class NumDays {
public:
    NumDays();
    NumDays(double hours);
    NumDays(NumDays& n);
    void setHours(double);
    void setDays(double);
    double getHours();
    double getDays();
    double operator+(NumDays&);
    double operator-(NumDays&);
    bool operator>(NumDays&);
    bool operator<(NumDays&);
    NumDays operator++();
    NumDays operator++(int);
    string toString();
private:
    double hours;
    double days;
    void calculateDays();
    void calculateHours();
};

#endif /* NUMDAYS_H */

