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
public class SavingsAccount extends CheckingAccount{
    public SavingsAccount(long accountNumber, double initialBalance, double interestRate){
        super(accountNumber, initialBalance, interestRate, 0);
    }
}
