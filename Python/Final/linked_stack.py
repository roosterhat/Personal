from abstract_stack import AbstractStack
from abstract_collection import AbstractCollection
from linked_structure import LinkedStructure

class LinkedStack(AbstractStack):
    def __init__(self, other = None):
        self._items = LinkedStructure(other)
        AbstractCollection.__init__(self)


if __name__ == "__main__":
    stack = LinkedStack([1, 2, 3])
    #print(stack.pop())
    stack.push(5)
    stack.push(1)
    stack.push(5)
    stack.push(1)
    print(stack)
    print(stack.peek())
    val = stack.pop()
    print("Item is "+ str(val) + " stack value is " + str(stack))
    stack.clear()
    print(stack)