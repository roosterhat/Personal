import math

def sphere(r):
    pi = math.pi
    diameter = r*2
    circumference = 2*pi*r
    surfaceArea = 4*pi*pow(r, 2)
    volume = 4/3*pi*pow(r, 3)
    return diameter, circumference, surfaceArea, volume

print("Sphere with radius 10:",sphere(10))

def weeklyPay(rate, hour, over):
    pay = rate*hour
    overPay = over*(1.5*rate)
    return pay+overPay

print("Weekly Pay:",weeklyPay(10,10,0))
print("Weekly Pay (with overtime):",weeklyPay(10,10,2))

def totalDistance(dropHeight,bi,bounces):
    distance = 0
    height = dropHeight
    for i in range(0,bounces):
       distance += height
       height *= bi
       distance += height
    return distance

print("Total Distance:",totalDistance(10,.6,2))

def approxPI(iter):
    pi = 0
    for i in range(1,iter):
        if i%2==0:
            pi -= 1/(1+(2*(i-1)))
        else:
            pi += 1/(1+(2*(i-1)))
    return pi

print("PI/4 =",approxPI(100))

def creditCalculator(price):
    balance = price
    downPay = price*.1
    annualRate = .12
    monthlyPay = (price - downPay)*.05
    month = 1
    balance -= downPay
    totalPay = 0
    print()
    print("Down Payment: $%.2f" % downPay)
    print("Annual Interest Rate: {:.1%}".format(annualRate))
    print("{:<6s}|{:^12s}|{:^12s}|{:^12s}|{:^12s}|{:>12s}".format
          ("Month", "Balance", "Interest", "Payment", "Principle", "remaining"))
    print("-"*71)
    while balance > 0:
        monthlyInt = balance * annualRate / 12
        principle = monthlyPay-monthlyInt
        if principle > balance:
            principle = balance
            monthlyPay = balance+monthlyInt
        newB = balance-principle
        print('{:>6d}|{:>12,.2f}|{:>12,.2f}|{:>12,.2f}|{:>12,.2f}|{:>12,.2f}'.format
              (month, balance, monthlyInt, monthlyPay, principle, newB))
        month += 1
        balance = newB
        totalPay += monthlyPay+monthlyInt
    print("Total Payed: %.2f" % totalPay)

creditCalculator(5000)

