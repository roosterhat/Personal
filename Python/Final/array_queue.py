from abstract_queue import AbstractQueue
from abstract_collection import AbstractCollection
from array import Array


class ArrayQueue(AbstractQueue):
    def __init__(self, other = None):
        self._items = Array(other)
        AbstractCollection.__init__(self)


if __name__ == "__main__":
    q = ArrayQueue([1, 2, 3])
    #print(stack.pop())
    q.push(5)
    q.push(1)
    q.push(5)
    q.push(1)
    print(q)
    print(q.peek())
    val = q.pop()
    print("Item is "+ str(val) + " queue front value is " + str(q))
    val = q.pop()
    print("Item is "+ str(val) + " queue front value is " + str(q))
    q.clear()
    q.push(5)
    print(q)