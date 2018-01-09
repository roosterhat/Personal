/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/* 
 * File:   Uppercase.cpp
 * Author: erik ostlind
 * jeo170030
 * 
 * Created on November 30, 2017, 1:51 PM
 */
#include <iostream>
#include "Uppercase.h"

void Uppercase::doFilter(ifstream& in, ofstream& out){
    if(in.is_open() && out.is_open()){
        in.seekg(0,in.beg);
        char c;
        while(!in.get(c).fail())
            out<<transform(c);
        out.close();
    }
}

char Uppercase::transform(char c){
    if(c>=97 && c<=122)
        return char(c-32);
    return c;        
}
