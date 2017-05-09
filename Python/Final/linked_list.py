from abstract_list import AbstractList
from linked_structure import LinkedStructure

class LinkedList(AbstractList):

    def __init__(self, col=None):
        self._items = LinkedStructure(col)
        AbstractList.__init__(self)



e = LinkedList()

e.add(10)
e.add(134)
e.add(11)
e.add(88)

print(e.__str__())