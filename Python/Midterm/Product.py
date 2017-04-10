import math
import Categories

class Product:
    def __init__(self,name,desc,price,category,delivery):
        self.name = str(name)
        self.description = str(desc)
        self.price = float(price)
        self.reviews = []
        if isinstance(category,Categories.Category):
            self.category = category
        else:
            self.category = Categories.BaseCategory()
        self.deliveryMethod = str(delivery)
        self.stock = 0

    def __str__(self):
        return "[Name] "+self.name+" [Category] "+self.category.name+ \
               " [Price] "+str(self.price)+" [Current Stock] "+str(self.stock)

    def restock(self,amount):
        self.stock += int(amount)

    def reduceStock(self,amount):
        self.stock -= int(amount)

    def addReview(self,r):
        self.reviews.append(str(r))

    def removeReview(self,r):
        self.reviews.remove(str(r))


class DigitalProduct(Product):
    def __init__(self,n,d,p,c):
        Product.__init__(self,n,d,p,c,"Digital Code")
        self.stock = math.inf

    def restock(self,amount):
        return None

class PhysicalProduct(Product):
    def __init__(self,n,d,p,c):
        Product.__init__(self,n,d,p,c,"Delivery")

class SubscriptionProduct(Product):
    def __init__(self,n,d,p,c):
        Product.__init__(self,n,d,p,c,"Activation Code")
        self.stock = math.inf

    def restock(self,amount):
        return None
