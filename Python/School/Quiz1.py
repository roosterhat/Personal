import math

def isfibo(num):
    l = 0
    c = 1
    for i in range(0,num):
        o = c
        c = l+c
        l = o
        if num==c:
            return True
    return False

def isfiboR(num):
    def fibor(l,c):
        o = c
        c = l+c
        l = o
        if(c>num):
            return False
        if(c==num):
            return True
        return fibor(l,c)
    return fibor(0,1)

def simpfibo(num):
    v1 = 5*pow(num,2)+4
    v2 = 5*pow(num,2)-4
    return math.sqrt(v1).is_integer() or math.sqrt(v2).is_integer()

print(isfibo(8))
print(isfiboR(34))
print(simpfibo(55))
print()

def reverseStr(str):
    new = ""
    for c in str:
        new = c + new
    return new

def reverseStrR(str):
    if(len(str)==0):
        return ""
    else:
        return str[len(str)-1] + reverseStrR(str[:len(str)-1])

def simpReverse(str):
    return str[len(str)::-1]

print(reverseStr("ABCDEFG"))
print(reverseStrR("ABCDEFG"))
print(simpReverse("ABCDEFG"))
print()

def remainder(num1,num2):
    q = 0
    div = num1
    while div>=num2:
        div -= num2
        q += 1
    print(num1,"/",num2,"=",q,"with remainder",div)

remainder(10,5)
remainder(23,3)
print()

def removex(str):
    new = ""
    for c in str:
        if c not in new:
            new = new + c
    return new

def removexR(str):
    def remove(n,c):
        if len(c)==0:
            return n
        if c[0] not in n:
            return remove(n+c[0],c[1:])
        else:
            return remove(n,c[1:])
    return remove("",str)

def simpremovex(str):
    return "".join(sorted(set(str), key=str.index))

print(removex("AAABBBCCCDEFG"))
print(removexR("AAABBBCCCDEFG"))
print(simpremovex("AAABBBCCCDEFG"))
print()

