class Category:
    def __init__(self,name):
        self.name = name
        self.parent = None
        self.sub = []

    def __str__(self):
        res = self.name
        for c in self.sub:
            res = res+"/"+c.name
        return res

    def __iter__(self):
        return self.sub.__iter__()

    def addSubCategory(self,category):
        if isinstance(category,Category):
            category.parent = self
            self.sub.append(category)

    def removeSubCategory(self,category):
        self.sub.remove(category)

    def hasParent(self):
        return self.parent is not None

    def hasSubCategory(self):
        return len(self.sub)>0

class BaseCategory(Category):
    def __init__(self):
        Category.__init__(self,"Base")