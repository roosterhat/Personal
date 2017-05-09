from abstract_list import AbstractList
from array import Array

class ArrayList(AbstractList):

    def __init__(self, col=None):
        self._items = Array(col)
        AbstractList.__init__(self)