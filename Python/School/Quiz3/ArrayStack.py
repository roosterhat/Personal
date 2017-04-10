class ArrayStack:
    def __init__(self):
        self.stack = list()

    def __iter__(self):
        return self.stack.__iter__()

    def __len__(self):
        return len(self.stack)

    def __str__(self):
        return str(self.stack)

    def isEmpty(self):
        return len(self.stack)==0

    def push(self,item):
        self.stack.insert(0,item)

    def pop(self):
        return self.stack.pop(0)

    def peek(self):
        return self.stack[0]


s = ArrayStack()
print(s)
for i in range(10):
    s.push(i)

print(s.peek())
print(s.pop())
print(s)
