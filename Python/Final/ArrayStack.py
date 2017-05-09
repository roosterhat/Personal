from AbstractStack import AbstractStack

class ArrayStack(AbstractStack):

    def __init__(self, size):
        AbstractStack.__init__(self)
        self.content = []
        for i in range(0,size):
            self.content.append(None)

        self.top = -1

    def add(self, element):
        if len(self.content)-2 >= self.top:
            self.top += 1
            self.content[self.top] = element
        else:
            print("No more space in stack!")

    def pop(self):
        print("Starting at: ", self.top)
        if self.top >= 0:
            tmp = self.content[self.top]
            self.content[self.top] = None
            self.top -= 1
            return tmp
        else:
            print("Stack is empty!")

    def peek(self, index):
        if self.__len__() <= index or index < 0:
            print("Out of bounds.")
        else:
            return self.content[(self.top - index)+1]

    def is_Empty(self):
        return self.top < 0

    def __len__(self):
        return self.top + 1

    def __str__(self):
        out = ""
        for i in range(0, self.__len__()):
            out += str(i) + " "
        return out

    def __eq__(self, other):
        if self is other:
            return True
        elif type(self) != type(other) or self.__len__() != other.len():
            return False

        for i in range(-1, self.top):
            if self.content[i+1] != other.content[i+1]:
                return False
        return True
