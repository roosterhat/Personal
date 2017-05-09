class Node():
    def __init__(self, element, prev_node=None, next_node=None):
        self._element = element
        self._prev_node = prev_node
        self._next_node = next_node

    def getElement(self):
        return self._element

    def getPrev(self):
        return self._prev_node

    def getNext(self):
        return self._next_node

    def makeNode(self, data):
        packet = Node(data, )
