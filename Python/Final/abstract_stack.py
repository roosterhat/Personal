from stack_interface import StackInterface
from abstract_collection import AbstractCollection


class AbstractStack(StackInterface, AbstractCollection):
    def pop(self):
        if self.is_empty():
            raise ValueError("Stack is empty")
        else:
            val = self._items[self._size - 1]
            self._items.remove(self._size - 1)
            self._size = len(self._items)
            return val

    def peek(self):
        if self.is_empty():
            raise ValueError("Stack is empty")
        else:
            return self._items[self._size - 1]

    def push(self, item):
        self._items.add(item)
        self._size = len(self._items)

    def clear(self):
        self._items.clear()
