from Collection.abstract_collection import AbstractCollection
from Collection.abstract_list import AbstractList
from Collection.array import Array


class ArraySortedList(AbstractList):

    def __init__(self, other=None):
        self._items = Array(other)
        AbstractList.__init__(self)

    def pop(self, i=0):
        if i == None: i = len(self) - 1
        if i < 0 or i >= len(self):
            raise IndexError("List index out of range")
        item = self._items[i]
        self._items.remove(i)
        self._size = len(self._items)
        return item

    def add(self, item):
        b = False
        if self.__len__() == 0:
            self._items.add(item)
            b = True
        else:
            for i in range(0, self._size):
                if item < self._items[i]:
                    self._items.insert(i, item)
                    self._size = len(self._items)
                    b = True
                    break
        if not b:
            self._items.add(item)
        self._size = len(self._items)
        self.inc_mod_count()

e = ArraySortedList()

e.add(20)
e.add(12)
e.add(55)
e.add(99)
e.add(11)
e.remove(11)
print(e.get_mod_count())
print(e.__len__())
print(e.__str__())