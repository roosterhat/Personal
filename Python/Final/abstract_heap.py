from heap_interface import heap_interface
from abstract_collection import AbstractCollection

class abstract_heap(heap_interface, AbstractCollection):

    def pop(self):
        if self.is_empty():
            raise Exception()
        self._size -= 1
        topItem = self._items[0]
        bottomItem = self._items.pop(len(self) - 1)
        if len(self._items) == 0:
            return bottomItem
        self._items[0] = bottomItem
        lastIndex = len(self) - 1
        curPos = 0
        while True:
            leftChild = 2 * curPos + 1
            rightChild = 2 * curPos + 2
            if leftChild > lastIndex:
                break
            if rightChild > lastIndex:
                maxChild = leftChild
            else:
                leftItem = self._items[leftChild]
                rightItem = self._items[rightChild]
                if leftItem < rightItem:
                    maxChild = leftChild
                else:
                    maxChild = rightChild
            maxItem = self._items[maxChild]
            if bottomItem <= maxItem:
                break
            else:
                self._items[curPos] = self._items[maxChild]
                self._items[maxChild] = bottomItem
                curPos = maxChild
        return topItem

    def add(self, item):
        self._size += 1
        self._items.add(item)
        curPos = self.__len__() - 1
        while curPos > 0:
            parent = (curPos - 1) // 2  # Integer quotient!
            parentItem = self._items[parent]
            if parentItem <= item:
                break
            else:
                self._items[curPos] = self._items[parent]
            self._items[parent] = item
            curPos = parent

    def peek(self):
        if self.is_empty():
            raise IndexError("List is empty!")
        else:
            return self._items[0]
