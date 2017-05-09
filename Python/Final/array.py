from abstract_collection import AbstractCollection


class Array(AbstractCollection):
    DEFAULT_SIZE = 10

    def __init__(self, other=None):
        if other is not None:
            if len(other) > self.DEFAULT_SIZE:
                self._items = [None] * len(other)
            else:
                self._items = [None] * self.DEFAULT_SIZE
        else:
            self._items = [None] * self.DEFAULT_SIZE

        self._capacity = len(self._items)
        AbstractCollection.__init__(self, other)

    def add(self, elem):
        self._expand()
        self._items[self._size] = elem
        self._size += 1

    def __iter__(self):
        index = 0
        for item in self._items:
            if index >= self._size:
                break
            yield item
            index += 1

    def __str__(self):
        out = "["
        started = False
        for item in self:
            if started:
                out += ","
            out += str(item)
            started = True
        out += "]"
        return out

    def __getitem__(self, index):
        if index > self._size or index < 0:
            raise IndexError("Index " + index + " is out of bound")
        else:
            return self._items[index]

    def __setitem__(self, index, value):
        if index > self._size or index < 0:
            raise IndexError("Index " + index + " is out of bound")
        else:
            self._items[index] = value

    def insert(self, index, value):
        if index > self._size or index < 0:
            raise IndexError("Index " + index + " is out of bound")
        else:
            self._expand()
            self._shift_right(1, index)
            self._items[index] = value
            self._size += 1

    def _expand(self):
        if self._size >= self._capacity:
            temp_capacity = self._capacity * 2
            temp_array = [None] * temp_capacity
            for i in range(0, self._size):
                temp_array[i] = self._items[i]
            self._items = temp_array

    def _shift_right(self, places, index):
        for i in range(self._size-1, index-1, -1):
            self._items[i+places] = self._items[i]
            self._items[i] = None

    def _shift_left(self, places, index):
        for i in range(index, self._size - places):
            self._items[i] = self._items[i+places]

    def remove(self, index):
        if index > self._size or index < 0:
            raise IndexError("Index " + index + " is out of bound")
        else:
            self._shift_left(1, index)
            self._size -= 1

    def __len__(self):
        return self._size

    def capacity(self):
        return self._capacity

    def clear(self):
        self._items = [None] * self.DEFAULT_SIZE
        self._size = 0
        self._capacity = self.DEFAULT_SIZE

if __name__ == "__main__":
    arr = Array([1, 3, 5, 2, 5, 6, 7, 8, 7, 6, 7, 6, 8, 8])
    print(arr)
    arr.insert(0, 10000)
    arr.remove(1)
    print(arr)
