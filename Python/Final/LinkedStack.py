from ListInterface import LinkedList

class ListStack:

    def __init__(self):
        self.content = LinkedList()

    def push(self, item):
        self.content.push(item)

    def pop(self):
        temp = None
        try:
            if self.content.__len__() > 1:
                temp = self.content.get_item_at_index(0)
                self.content.head = temp.getNext()
            elif self.content.__len__() == 1:
                temp = self.content.get_item_at_index(0)
                self.content.head = None
            else:
                raise NotADirectoryError("List is empty!")
        except NotADirectoryError as e:
            print(str(e))
        return temp

    def len(self):
        return self.content.__len__()

    def peek(self, index):
        if index < 1 or index > self.content.__len__():
            print("Can't peek at what's not there!")
            return None
        else:
            return self.content.get_item_at_index(index - 1)

    def is_Empty(self):
        return self.content.__len__() == 0

    def __str__(self):
        out = ""
        current = self.content.head
        while current is not None:
            out += str(current.getElement()) + " "
            current = current.getNext()
        return out

    def __eq__(self, other):
        if self is other:
            return True
        elif self.len() != other.len() or type(self) != type(other):
            return False
        else:
            current = self.content.head
            other_cur = other.content.head
            while current is not None:
                if other_cur.getElement() != current.getElement():
                    return False
                other_cur = other_cur.getNext()
                current = current.getNext()
        return True
