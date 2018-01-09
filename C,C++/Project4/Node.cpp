/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/* 
 * File:   Node.cpp
 * Author: erik ostlind
 * ID: jeo170030
 * 
 * Created on November 1, 2017, 12:56 PM
 */
#include <string>
#include <math.h>
#include <regex>
#include "Value.h"
#include "Node.h"
#include "Trig.h"

using namespace std;

Node::Node(double c, double e, double fc, Function* t){
    coefficient = c;
    exponent = e;
    function = t;
    functionCoefficient = fc;
}
Node::Node(int c, int e, int fc, Function* t){
    coefficient = double(c);
    exponent = double(e);
    function = t;
    functionCoefficient = double(fc);
}
string Node::getValue(){
    string res = "";
    if(function!=nullptr){
        if(floor(functionCoefficient)==functionCoefficient)
            res+=formatFloat(functionCoefficient);
        else
            res+="("+realToFraction(functionCoefficient)+")";
        res+=function->getValue();
    }
    if(floor(coefficient)==coefficient)
        res+=formatFloat(coefficient);
    else
        res+="("+realToFraction(coefficient)+")";
    if(exponent!=0){
        res+="x";
        if(exponent!=1)
            res+="^"+formatFloat(exponent);
    }
    return res;
}

bool Node::equal(string val){
    return getValue()==val;
}

void Node::derive(){
    coefficient *= exponent;
    if(function!=nullptr){
        function->derive(this);
        functionCoefficient *= coefficient;
    }
    else if(exponent!=0)
        exponent -= 1.0f;
    
}
string Node::formatFloat(double f,int n){
    string s = to_string(f);
    return regex_replace(s.substr(0,s.find(".")+1+n),regex("0{1,"+to_string(n-1)+"}$"),"");
}

string Node::realToFraction(double value, double accuracy) 
//code from https://www.google.ch/url?sa=t&rct=j&q=&esrc=s&source=web&cd=3&cad=rja&uact=8&ved=0ahUKEwjv0MfQgofNAhWEuhoKHfZSCEEQFggoMAI&url=https%3A%2F%2Fwww.maa.org%2Fsites%2Fdefault%2Ffiles%2Fpdf%2Fupload_library%2F22%2FAllendoerfer%2F1982%2F0025570x.di021121.02p0002y.pdf&usg=AFQjCNHqY1RApwZ2dQ47xG913Dx_caagJQ
{
    double sign = signbit(value)==0 ? 1.0 : -1.0;
    double g(abs(value));
    unsigned long long num;
    unsigned long long den;
    unsigned long long a(0);
    unsigned long long b(1);
    unsigned long long c(1);
    unsigned long long d(0);
    unsigned long long s;
    unsigned int iter(0);
    do {
        s = floor(g);
        num = a + s*c;
        den = b + s*d;
        a = c;
        b = d;
        c = num;
        d = den;
        g = 1.0/(g-s);
        if(accuracy>abs(sign*num/den-value))
            return to_string(long(sign*num))+"/"+to_string(den); 
    } while(iter++<1e6);
}
