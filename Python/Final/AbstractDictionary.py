from DictionaryInterface import DictionaryInterface


class AbstractDictionary(DictionaryInterface):
    def __setitem__(self, key, value):
        self.add(Entry(key, value))

    def getItem(self, key):
        for entry in self:
            if entry.key == key:
                return entry.item

    def getKey(self, item):
        for entry in self:
            if entry.item == item:
                return entry.key

    def pop(self):
        val = self._data
        self.remove(self._data)
        return val


class Entry:
    def __init__(self, key, value):
        self.key = key
        self.value = value

    def __str__(self):
        return "key: " + str(self.key) + ", item: " + str(self.value)
