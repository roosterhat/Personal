from queue_interface import QueueInterface
from abstract_collection import AbstractCollection


class AbstractQueue(QueueInterface, AbstractCollection):

    def pop(self):
        if self.is_empty():
            raise ValueError("Stack is empty")
        else:
            val = self._items[0]
            self._items.remove(0)
            self._size = len(self._items)
            return val

    def peek(self):
        if self.is_empty():
            raise ValueError("Stack is empty")
        else:
            return self._items[0]

    def push(self, item):
        self._items.add(item)
        self._size = len(self._items)

    def clear(self):
        self._items.clear()