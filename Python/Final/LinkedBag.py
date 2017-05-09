from AbstractBag import AbstractBag
from linked_structure import LinkedStructure


class LinkedBag(AbstractBag,LinkedStructure):
    def __init__(self,value=None,comp=lambda x,y:x==y):
        LinkedStructure.__init__(value,comp=comp)

    def getAmount(self,item):
        count = 0
        if self.comparator(item,self[0]):
            count += 1
        if self.hasTail():
            count += self.getTail().getAmount(item)
        return count



