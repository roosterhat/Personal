/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package homework3;

import java.util.Calendar;

/**
 *
 * @author eriko
 */
public abstract class Account {
    protected long accountNumber;
    protected double balance, annualInterestRate;
    final protected Calendar dateCreated;
    
    public Account(long accountNumber, double initialBalance, double interestRate){
        this.accountNumber = accountNumber;
        balance = initialBalance;
        this.annualInterestRate = interestRate;
        dateCreated = Calendar.getInstance();
    }
    
    public long getAccountNumber(){return accountNumber;}
    public double getBalance(){return balance;}
    public double getAnnualInterestRate(){return annualInterestRate;}
    public double getMonthlyInterestRate(){return annualInterestRate/12;}
    public double getMonthlyInterest(){return getMonthlyInterestRate()*balance;}
    public Calendar getDateCreated(){return dateCreated;}
    
    public void setAnnualInterestRate(double rate){
        annualInterestRate = rate;
    }
    
    public double deposite(double amount){
        if(amount>0)
            balance += amount;
        return balance;
    }
    
    public abstract double withdraw(double amount);
    
    public String toString(){
        return accountNumber+" $"+balance;
    }
}
