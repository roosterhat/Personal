from abstract_collection import AbstractCollection
from abc import ABCMeta


class AbstractList(AbstractCollection):
    __metaclass__ = ABCMeta

    def __init__(self, col=None):
        self._modCount = 0
        AbstractCollection.__init__(self, col)

    def __iter__(self):
        for i in self._items:
            yield i

    def __getitem__(self, item):
        self._items.__getitem__(item)

    def __str__(self):
        tmp = ""
        for i in self:
            tmp += str(i) + " "
        return tmp

    def clear(self):
        self._items.clear()
        self._size = len(self._items)

    def get_mod_count(self):
        return self._modCount

    def inc_mod_count(self):
        self._modCount += 1

    def add(self, item):
        self._items.add(item)
        self._size = len(self._items)
        self.inc_mod_count()

    def remove(self, item):
        position = self.index(item)
        self._items.remove(position)
        self._size = len(self._items)
        self.inc_mod_count()

    def index(self, item):
        position = 0
        for data in self:
            if data == item:
                return position
        else:
            position += 1
        if position == len(self):
            raise ValueError(str(item) + " not in list.")