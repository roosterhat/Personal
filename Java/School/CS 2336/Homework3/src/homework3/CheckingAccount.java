/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package homework3;

/**
 *
 * @author eriko
 */
public class CheckingAccount extends Account{
    private double overDraftLimit;
    public CheckingAccount(long accountNumber, double initialBalance, double interestRate, double overDraftLimit){
        super(accountNumber, initialBalance, interestRate);
        this.overDraftLimit = overDraftLimit;
    }
    
    public double withdraw(double amount){
        double before = balance;
        if(amount>0)    
            balance = Math.max(-overDraftLimit, balance-amount);    //makes sure the balance doesnt exceed the overdraft limit 
        return before-balance;  //returns the amount succesfully withdrawn
    }
}
