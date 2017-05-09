from ArrayBag import ArrayBag

class ArraySet(ArrayBag):
    def __init__(self):
        ArrayBag.__init__(self)

    def add(self,item):
        if not self.__contains__(item):
            ArrayBag.add(self,item)

    def combine(self,set):
        if isinstance(set,ArrayBag):
            for i in set:
                self.add(i)