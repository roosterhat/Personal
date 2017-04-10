import Categories
import Product

class Catalogue:
    def __init__(self,*args):
        if len(args)>0:
            if isinstance(args[0],list):
                for c in args[0]:
                    if isinstance(c, Categories.Category):
                        self.categories[c.name] = []
            if isinstance(args[0], Categories.Category):
                for c in args:
                    if isinstance(c, Categories.Category):
                        self.categories[c.name] = []
        else:
            self.categories = {}

    def __str__(self):
        res = ""
        for c in self.categories:
            res += c.name + "\n ->   "
            for p in self.categories.get(c):
                res += p.name + ","
            res = res[:len(res)-1]+"\n"
        return res

    def __iter__(self):
        return self.categories.__iter__()

    def __contains__(self,obj):
        if isinstance(obj,Product.Product):
            for c in self.categories:
                if self.categories.get(c).__contains__(obj):
                    return True
            return False
        else:
            return self.categories.__contains__(obj)

    def addCategory(self,category):
        if isinstance(category, Categories.Category) and \
                        category not in self.categories:
            self.categories[category] = []
        else:
            raise "Input must be a Category"

    def removeCategory(self,category):
        del self.categories[category]


    def addProduct(self,product):
        if isinstance(product,Product.Product):
            if product.category in self.categories:
                self.categories[product.category].append(product)
            else:
                curr = product.category
                while curr.hasParent():
                    curr = curr.parent
                    self.addCategory(curr)
                if isinstance(product.category, Categories.Category):
                    self.categories[product.category] = [product]
        else:
            raise "Input but be a Product"

    def removeProduct(self,product):
        for c in self.categories:
            if self.categories.get(c).__contains__(product):
                self.categories.get(c).remove(product)

    def getProducts(self):
        res = []
        for c in self:
            for p in self.categories.get(c):
                res.append(p)
        return res
