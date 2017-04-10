import Log
from Product import *
from Categories import *
from User import *
from Catalogue import *
import GUI

class store:
    def __init__(self):
        self.curruser = None

        mainAdmin = Admin("headadmin","password")
        testuser = Customer("test","test")
        self.users = [mainAdmin,testuser]

        self.catalogue = Catalogue()

        electronics = Category("Electronics")
        tv = Category("TV")
        electronics.addSubCategory(tv)
        digital = Category("Digital")

        samsungtv = PhysicalProduct("Samsung TV","55' HD OLED TV",999.99,tv)
        samsungtv.restock(20)
        iphone = PhysicalProduct("IPhone 6","Iphone by Apple",800,electronics)
        iphone.restock(100)
        samsung = PhysicalProduct("Samsung Galaxy 7","Galaxy by Samsung",750,electronics)
        samsung.restock(100)
        netflix = SubscriptionProduct("Netflix Membership","Membership code",9,digital)

        self.catalogue.addProduct(samsungtv)
        self.catalogue.addProduct(iphone)
        self.catalogue.addProduct(samsung)
        self.catalogue.addProduct(netflix)

        self.productLog = Log.DynamicLog("Name","Description","Price","Category","Stock")
        self.productLog.newEntry(samsungtv,Log.ProductConverter())
        self.productLog.newEntry(iphone,Log.ProductConverter())
        self.productLog.newEntry(samsung,Log.ProductConverter())
        self.productLog.newEntry(netflix,Log.ProductConverter())

        self.eventLog = Log.DynamicLog("Action","Source","Details")

        self.salesLog = Log.DynamicLog("Item","Quantity","New Stock")

        self.userLog = Log.DynamicLog("Username","Password","Rank")
        self.userLog.newEntry(mainAdmin,Log.UserConverter())
        self.userLog.newEntry(testuser,Log.UserConverter())

        self.GUI = GUI.GUI(self)

    def updateProductLog(self):
        self.productLog.clearLog()
        for p in self.catalogue.getProducts():
            self.productLog.newEntry(p,Log.ProductConverter())
        self.eventLog.newEntry("Update Log",self.curruser.getUsername(),"Product Log")

    def updateUserLog(self):
        self.userLog.clearLog()
        for user in self.users:
            self.userLog.newEntry(user,Log.UserConverter())
        self.eventLog.newEntry("Update Log",self.curruser.getUsername(),"User Log")

    def checkUsername(self,user):
        for u in self.users:
            if u.getUsername() == user:
                return True
        return False

    def isFloat(self,f):
        if isinstance(f,str):
            for num in f.split("."):
                if not num.isdigit():
                    return False
            return True
        else:
            return isinstance(f,float)

    def getUser(self,user):
        for u in self.users:
            if u.getUsername() == user:
                return u
        return None

    def login(self,username,password):
        if self.checkUsername(username):
            u = self.getUser(username)
            if u is not None and u.checkPass(password):
                self.eventLog.newEntry("Login",username,"")
                self.curruser = u
                return True
        return False

    def logout(self):
        if self.curruser is not None:
            self.eventLog.newEntry("Logout",self.curruser.getUsername(),"")
            self.curruser = None

    def newUser(self,username,password,rank):
        username = username.rstrip()
        if not username=="":
            if not self.checkUsername(username):
                if rank=="Customer":
                    self.users.append(Customer(username,password))
                    self.eventLog.newEntry("New User","System",username+" Rank: "+rank)
                elif rank=="Admin":
                    self.users.append(Admin(username,password))
                    self.eventLog.newEntry("New User",self.curruser.getUsername(),username+"/ "+rank)
                self.userLog.newEntry(self.users[len(self.users)-1],Log.UserConverter())
                return True
        return False


    def deleteUser(self,username):
        user = self.getUser(username)
        if self.users.__contains__(user):
            self.eventLog.newEntry("Delete User",self.curruser.getUsername(),user.getUsername())
            self.users.remove(user)
            self.updateUserLog()
            return True
        else:
            return False

    def getProduct(self,item):
        for p in self.catalogue.getProducts():
            if str.upper(item)==str.upper(p.name):
                return p
        return None

    def getCategory(self,cat):
        for c in self.catalogue:
            if c.name.upper() == cat.upper():
                return c
        return None

    def addProduct(self,item):
        prod = self.getProduct(item)
        if prod is not None:
            if prod.stock>0:
                self.curruser.addItemCart(prod)
                return True
        return False

    def deleteProduct(self,item):
        temp = len(self.curruser.getCart())
        self.curruser.removeItemCart(self.getProduct(item))
        if temp==len(self.curruser.getCart()):
            return False
        else:
            return True

    def clearCart(self):
        self.curruser.clearCart()

    def addFunds(self,amount):
        if amount.isdigit():
            self.eventLog.newEntry("Added Funds",self.curruser.getUsername(),"$"+amount)
            self.curruser.addFunds(int(amount))

    def newProduct(self,name,desc,price,cat,type):
        if self.getProduct(name) is not None:
            print("Product Name already exists")
            return False

        if desc is None:
            print("Description cannot be None")
            return False

        if self.isFloat(price):
            price = float(price)
        else:
            print("Price must be a number")
            return False

        cat = self.getCategory(cat)
        if cat is None:
            print("Category does not exist")
            return False

        type = type.upper()
        if type == "PHYSICAL" or type == "DIGITAL" or type == "SUBSCRIPTION":
            prod = None
            if type == "PHYSICAL":
                prod = PhysicalProduct(name,desc,price,cat)
            elif type == "DIGITAL":
                prod = DigitalProduct(name,desc,price,cat)
            elif type == "DIGITAL":
                prod = SubscriptionProduct(name,desc,price,cat)
            self.catalogue.addProduct(prod)
            self.productLog.newEntry(prod,Log.ProductConverter())
            self.eventLog.newEntry("New Product",self.curruser.getUsername(),name)
            return True
        else:
            print("Unknown Type")
            return False

    def addCategory(self,cat,parent):
        if self.getCategory(cat) is None:
            cat = Category(cat)
            if not parent == "None":
                parent = self.getCategory(parent)
                if parent is not None:
                    parent.addSubCategory(cat)
                else:
                    return False
            self.catalogue.addCategory(cat)
            self.eventLog.newEntry("Category Added",self.curruser.getUsername(),cat.name)
            return True
        else:
            return False

    def removeCategory(self,cat):
        cat = self.getCategory(cat)
        if cat is not None:
            self.catalogue.removeCategory(cat)
            self.eventLog.newEntry("Category Deleted",self.curruser.getUsername(),cat.name)
            return True
        else:
            return False

    def removeProduct(self,prod):
        prod = self.getProduct(prod)
        if prod is not None:
            self.catalogue.removeProduct(prod)
            self.eventLog.newEntry("Product Deleted",self.curruser.getUsername(),prod.name)
            self.updateProductLog()
            return True
        else:
            return False

    def restock(self,prod,stock):
        prod = self.getProduct(prod)
        if prod is not None:
            if stock.isdigit():
                stock = int(stock)
                prod.restock(stock)
                self.eventLog.newEntry("Restock Product",self.curruser.getUsername(),prod.name+" ["+str(prod.stock)+"]")
                self.updateProductLog()
                return True

        return False

    def preformSale(self,items):
        void = {}
        total = 0.00
        for i in items:
            if items.get(i)<=i.stock:
                i.reduceStock(items.get(i))
                self.salesLog.newEntry(i.name,items.get(i),i.stock)
                total += items.get(i) * i.price
            elif i.stock==0:
                void[i] = items.get(i)
            else:
                self.salesLog.newEntry(i.name,i.stock,i.stock)
                void[i] = items.get(i)-i.stock
                total += i.stock * i.price
                i.reduceStock(i.stock)
        self.curruser.removeFunds(total)
        self.eventLog.newEntry("Sale",self.curruser.getUsername(),"$"+str(total))
        self.updateProductLog()
        return void

    def loadData(self,path):
        try:
            file = open(path,"r")
            self.eventLog.newEntry("Import Attempt",self.curruser.getUsername(),path)
            imports = list()
            for line in file:
                try:
                    words = line.rstrip("\n").rstrip().split(",")
                    identifier = words[0].upper()
                    if identifier == "PHYSICAL" or identifier == "DIGITAL" or identifier == "SUBSCRIPTION":
                        if len(words[1:]) >= 4:
                            name = words[1]
                            if self.getProduct(name) is None:
                                desc = words[2]
                                price = float(words[3])
                                cat = self.getCategory(words[4])
                                if cat is not None:
                                    if identifier == "PHYSICAL":
                                        prod = PhysicalProduct(name,desc,price,cat)
                                    elif identifier == "DIGITAL":
                                        prod = DigitalProduct(name,desc,price,cat)
                                    elif identifier == "SUBSCRIPTION":
                                        prod = SubscriptionProduct(name,desc,price,cat)
                                    if len(words[1:])==5:
                                        if words[5].isdigit():
                                            prod.restock(int(words[5]))
                                    self.catalogue.addProduct(prod)
                                    self.productLog.newEntry(prod,Log.ProductConverter())
                                    self.eventLog.newEntry("New Product","Import",name)
                    elif identifier == "CATEGORY":
                        if len(words[1:])>=1:
                            name = words[1]
                            cat = self.getCategory(name)
                            if cat is None:
                                cat = Category(name)
                                if len(words[1:])==2:
                                    parent = self.getCategory(words[2])
                                    if parent is not None:
                                        parent.addSubCategory(cat)
                                self.catalogue.addCategory(cat)
                                self.eventLog.newEntry("Category Added","Import",cat.name)
                    elif identifier == "USER":
                        if len(words[1:])==3:
                            username = words[1]
                            password = words[2]
                            rank = words[3].upper()
                            if rank == "ADMIN" or rank == "CUSTOMER":
                                if rank == "ADMIN":
                                    user = Admin(username,password)
                                elif rank == "CUSTOMER":
                                    user = Customer(username,password)
                                self.users.append(user)
                                self.eventLog.newEntry("New User","Import",username+"/ "+rank)
                                self.userLog.newEntry(user,Log.UserConverter())
                    imports.append(line)
                except:
                    print("Cant read line")
            return imports
        except (IOError):
            return list()



store()



