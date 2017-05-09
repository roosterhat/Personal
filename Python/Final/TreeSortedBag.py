from AbstractBag import AbstractBag
from TreeStructure import TreeStructure

class TreeSortedBag(TreeStructure, AbstractBag):
    def __init__(self,item=None,comparator=lambda x,y: x-y):
        TreeStructure.__init__(self,other=item,comp=comparator)

    def getAmount(self,item):
        amount = 0
        for i in self.inorder():
            if self.comparator(item,i)==0:
                amount += 1
        return amount
