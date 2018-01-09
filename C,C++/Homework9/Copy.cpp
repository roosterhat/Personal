/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/* 
 * File:   Copy.cpp
 * Author: erik ostlind
 * jeo170030
 * 
 * Created on November 30, 2017, 1:56 PM
 */

#include "Copy.h"

void Copy::doFilter(ifstream& in, ofstream& out){
    if(in.is_open() && out.is_open()){
        in.seekg(0,in.beg);
        char c;
        while(!in.get(c).fail())
            out<<c;
        out.close();
    }
}

