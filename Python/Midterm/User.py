import Product
import math

class User:
    def __int__(self,u,p,r):
        if isinstance(u and p,str):
            self._username = u
            self._password = p
            self._rank = r
            self._wallet = 0
            self._cart = []
        else:
            raise Exception("Username and Password must be strings")

    def getUsername(self):
        return self._username

    def getRank(self):
        return self._rank

    def getPassword(self):
        return "*"*len(self._password)

    def setUsername(self,u):
        if isinstance(u,str):
            self._username = u

    def setPassword(self,p):
        if isinstance(p,str):
            self._username = p

    def checkPass(self,p):
        return self._password == p

    def addItemCart(self,product):
        if isinstance(product,Product.Product):
            self._cart.append(product)

    def removeItemCart(self,product):
        self._cart.remove(product)

    def getCart(self):
        return self._cart

    def clearCart(self):
        self._cart.clear()

    def getFunds(self):
        return self._wallet

    def addFunds(self,amount):
        if (isinstance(amount,int) or isinstance(amount,float)) and amount>0:
            self._wallet += amount

    def removeFunds(self,amount):
        if (isinstance(amount,int) or isinstance(amount,float)) and amount>0:
            self._wallet -= amount



class Admin(User):
    def __init__(self,n,p):
        User.__int__(self,n,p,"Admin")
        self._wallet = math.inf



class Customer(User):
    def __init__(self,n,p):
        User.__int__(self,n,p,"Customer")

