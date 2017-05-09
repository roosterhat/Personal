from ArraySortedBag import ArraySortedBag
from ArrayBag import ArrayBag

class ArraySortedSet(ArraySortedBag):
    def __init__(self,comparator=None):
        ArraySortedBag.__init__(self,comparator)

    def __add__(self, other):
        self.combine(other)

    def add(self,item):
        if not self.__contains__(item):
            ArraySortedBag.add(self,item)

    def combine(self,set):
        if isinstance(set,ArrayBag):
            for i in set:
                self.add(i)