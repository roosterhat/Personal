from abstract_heap import abstract_heap
from array import Array
from abstract_collection import AbstractCollection

class ArrayHeap(abstract_heap):

    def __init__(self):
        self._items = Array(100)
        abstract_heap.__init__(self)

a = ArrayHeap()
print(isinstance(a,AbstractCollection))