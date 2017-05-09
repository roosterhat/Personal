class AbstractCollection:

    def __init__(self, other=None):
            self._size = 0
            if other is not None:
                if isinstance(other,list):
                    for item in other:
                        self.add(item)
                else:
                    self.add(other)

    def __len__(self):
        return self._size

    def __add__(self, other):
        for item in other:
            self.add(item)

    def __str__(self):
        out = "["
        index = 0
        for item in self._items:
            if index != 0:
                out += ","
            out += str(item)
            index += 1
        out += "]"
        return out

    def __eq__(self, other):
        if self is other:
            return True
        elif type(self) != type(other) or len(self) != len(other):
            return False
        else:
            otherIter = iter(other)
            for item in self:
                if item != next(otherIter):
                    return False
            return True

    def is_empty(self):
        return self._size == 0


