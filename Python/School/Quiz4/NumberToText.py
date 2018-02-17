import random


class NumberConverter:
    def __init__(self):
        self.LARGEST = 1000000000000000
        self.zero = "zero"
        self.ones = ["one", "two", "three", "four", "five", "six", "seven", "eight", "nine"]
        self.tens = ["ten", "twenty", "thirty", "forty", "fifty", "sixty", "seventy", "eighty", "ninety"]
        self.hundreds = []
        for i in range(0, 9):
            self.hundreds.append(self.ones[i] + " hundred")
        self.numbers = [self.hundreds, self.tens, self.ones]
        self.teens = ["eleven", "twelve", "thirteen", "fourteen", "fifteen", "sixteen", "seventeen", "eighteen","nineteen"]
        self.larger = ["thousand", "million", "billion", "trillion"]

    def numberToWord(self, number):
        number = int(str(number).split(".")[0])
        if number <= 0:
            return self.zero
        elif number > self.LARGEST:
            return "Number is too large"
        else:
            return self._converterHelper(number, 0)

    def _converterHelper(self, number, count):
        num = int(str(number)[-3:])
        word = ""
        if not num == 0:
            for x in range(0, len(str(num))):
                if (len(str(num)) == 3 and x == 1) or (len(str(num)) == 2 and x == 0):
                    if 10 < int(str(num)[x:x + 2]) < 20:
                        word += self.teens[(num % 10) - 1] + " "
                        break
                if not int(str(num)[x:x + 1]) == 0:
                    word += self.numbers[x + (3 - len(str(num)))][int(str(num)[x:x + 1]) - 1] + " "
            if count > 0:
                word += self.larger[count - 1] + " "
        if len(str(number)) > 3:
            return self._converterHelper(str(number)[:-3], count + 1) + word
        else:
            return word


n = NumberConverter()

print(n.numberToWord(40411.12))
print(n.numberToWord(12012))
for x in range(10):
    num = 1
    for x in range(random.randint(1, 10)):
        num *= random.randint(1, 100);
    num += random.randint(0, 10000);
    print(str(num) + ": " + n.numberToWord(num))
