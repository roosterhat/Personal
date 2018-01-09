/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/* 
 * File:   main.cpp
 * Author: erik ostlind, seth bedford
 *jeo170030
 *sab170730
 *
 * Created on November 16, 2017, 1:10 PM
 */

#include <cstdlib>
#include <iostream>

#include "NumDays.h"

using namespace std;

/*
 * 
 */
int main() {
    NumDays n1(43);
    NumDays n2(n1);
    n2.setHours(123.0);
    cout<<"n1+n2 = "<<(n1+n2)<<endl;
    cout<<"n1-n2 = "<<(n1-n2)<<endl;
    cout<<"n1<n2 = "<<(n1<n2)<<endl;
    cout<<"n1>n2 = "<<(n1>n2)<<endl;
    cout<<"Before Prefix: "<<n1.toString()<<endl;
    cout<<(++n1).toString()<<endl;
    cout<<"Before Postfix: "<<n1.toString()<<endl;
    cout<<(n1++).toString()<<endl;
    cout<<"After Postfix: "<<n1.toString()<<endl;
    return 0;
}

