from AbstractDictionary import AbstractDictionary
from AbstractDictionary import Entry
from linked_structure import LinkedStructure


class LinkedDict(AbstractDictionary, LinkedStructure):
    def __init__(self, key=None, value=None,comp=lambda x, y: x == y.key):
        if key is not None:
            LinkedStructure.__init__(self, Entry(key, value), comp=comp)
        else:
            LinkedStructure.__init__(self, comp=comp)

    def __getitem__(self, key):
        return LinkedStructure.__getitem__(self, self.index(key))

    def add(self, entry):
        if isinstance(entry, Entry):
            LinkedStructure.add(self, entry)

    def insert(self, index, entry):
        if isinstance(entry, Entry):
            LinkedStructure.insert(index, entry)

