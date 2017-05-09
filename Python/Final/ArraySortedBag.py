from ArrayBag import ArrayBag

class ArraySortedBag(ArrayBag):
    def __init__(self,comparator=None):
        ArrayBag.__init__(self)
        if comparator is None:
            self.comparator = lambda x,y: x-y
        else:
            self.comparator = comparator

    def add(self,item):
        index = len(self)
        for i in range(len(self)):
            if self.comparator(item,self._bag[i])<0:
                index = i
                break
        self._bag.insert(index,item)


