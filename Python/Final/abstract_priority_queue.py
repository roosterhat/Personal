from abstract_queue import AbstractQueue


class PriorityNode:
    def __init__(self, data, priority = 0):
        self.data = data
        self.priority = priority

    def __str__(self):
        return str(self.data)


class AbstractPriorityQueue(AbstractQueue):

    def push(self, elem):
        if self._size == 0:
            self._items.add(elem)
        else:
            index = self._size
            for i in range(self._size-1, -1, -1):
                if elem.priority <= self._items[i].priority:
                    break
                index -=1
            self._items.insert(index, elem)
        self._size += 1