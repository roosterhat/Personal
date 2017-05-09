
class ArrayPQueue:

    def __init__(self, cap=50):
        self.content = []
        for i in range(0, cap):
            self.content.append(None)
        self.items = 0

    def is_Empty(self):
        for i in self.content:
            if i is not None:
                return False
        return True

    def insert(self, key):
        if self.items > len(self.content):
            print("Overflow!")
        else:
            self.content[self.items] = key
            self.items += 1

    def max(self):
        if self.items == 0:
            return None
        max = -2147000000
        for i in range(0,self.items):
            if self.content[i] > max:
                max = self.content[i]
        return max

    def popMax(self):
        if self.items == 0:
            return None
        max = -2147000000
        index = 0
        for i in range(0, self.items):
            if self.content[i] > max:
                max = self.content[i]
                index = i

        tmp = self.content[self.items-1]
        self.content[self.items] = None
        self.content[index] = tmp
        self.items -= 1

        return max

    # call real index (from 0 >> len - 1)
    def peek(self, index):
        if index < 0 or index > self.items-1:
            print("Out of bounds!")
        else:
            return self.content[index]

    def __len__(self):
        return self.items

    def __str__(self):
        string = ""
        for i in range(0, self.__len__()):
            string += str(self.content[i]) + " "
        return string

    def __eq__(self, other):
        if self is other:
            return True
        if self.__len__() != other.__len__() or type(self) != type(other):
            return False

        temp = self
        other_temp = other
        while temp.__len__() > 0:
            s_tmp = temp.popMax()
            o_tmp = other_temp.popMax()
            if s_tmp != o_tmp:
                return False
        return True


