from AbstractBag import AbstractBag

class ArrayBag(AbstractBag):
    def __init__(self):
        self._bag = []

    def __len__(self):
        return len(self._bag)

    def __str__(self):
        return str(self._bag)

    def __contains__(self, item):
        return self._bag.__contains__(item)

    def __iter__(self):
        return self._bag.__iter__()

    def add(self,item):
        self._bag.append(item)

    def delete(self,item):
        self._bag.remove(item)

    def getAmount(self,item):
        count = 0
        for i in self:
            if i==item:
                count += 1
        return count