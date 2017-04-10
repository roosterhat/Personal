from tkinter import *
import User
import Product
import main
import copy
from tkinter.filedialog import askopenfilename


class GUI:
    def __init__(self,m):
        self.x = 655
        self.y = 400
        self.canvas = Tk()
        self.canvas.geometry("%dx%d" % (self.x,self.y))
        self._main = m
        self.setView(self.main)
        self.canvas.mainloop()


    def setView(self,view):
        for child in self.canvas.winfo_children():
            child.destroy()
        view()

    def makeentry(self,parent, caption, width=None, **options):
        group = LabelFrame(parent,text=caption,padx=5,pady=5)
        entry = Entry(group, **options).pack()
        if width:
            entry.config(width=width)
        return group

    def main(self):
        self._main.logout()
        def logincall():
            self.setView(self.login)
        def newcall():
            self.setView(self.newUser)

        Label(self.canvas,text="Welcome").pack()
        b = Button(self.canvas,text="Login",command=logincall)
        b.config(width=10)
        b.pack()
        b2 = Button(self.canvas,text="New User",command=newcall)
        b2.config(width=10)
        b2.pack()

    def login(self):
        def callback():
            if len(msgframe.winfo_children())==1:
                msgframe.winfo_children()[0].destroy()
            if self._main.login(user.winfo_children()[0].get(),password.winfo_children()[0].get()):
                self.setView(self.header)
            else:
                Label(msgframe,text="Incorrect Username/Password",fg="red").pack()

        def back():
            self.setView(self.main)
        frame = Frame(self.canvas)
        Label(frame,text="Log In").grid(row=0, column=0, columnspan=2)
        user = self.makeentry(frame, "Username:")
        user.grid(row=1, column=0)
        password = self.makeentry(frame, "Password:", show="*")
        password.grid(row=1, column=1)
        b=Button(frame,text="Login",command=callback)
        b.config(width=10)
        b.grid(row=2, column=0,columnspan=2)
        b2 = Button(frame,text="Back",command=back)
        b2.config(width=10)
        b2.grid(row=3, column=0,columnspan=2)
        frame.pack()
        msgframe = Frame(self.canvas)
        msgframe.pack()

    def newUser(self):
        def callback():
            if len(msgframe.winfo_children())==1:
                msgframe.winfo_children()[0].destroy()
            if not user.winfo_children()[0].get()=="":
                if self._main.newUser(user.winfo_children()[0].get(),password.winfo_children()[0].get(),"Customer"):
                    Label(msgframe,text="Account Created").pack()
                else:
                    Label(msgframe,text="Account Name already exists",fg="red").pack()
            else:
                Label(msgframe,text="You must fill the required fields first",fg="red").pack()

        def back():
            self.setView(self.main)

        frame = Frame(self.canvas)
        Label(frame,text="New User").grid(row=0, column=0,columnspan=2)
        user = self.makeentry(frame, "Username:")
        user.grid(row=1, column=0)
        password = self.makeentry(frame, "Password:", show="*")
        password.grid(row=1, column=1)
        b=Button(frame,text="Create",command=callback)
        b.config(width=10)
        b.grid(row=2, column=0,columnspan=2)
        b2 = Button(frame,text="Back",command=back)
        b2.config(width=10)
        b2.grid(row=3, column=0,columnspan=2)
        frame.pack()
        msgframe = Frame(self.canvas)
        msgframe.pack()

    def newAdmin(self):
        def callback():
            if len(msgframe.winfo_children())==1:
                msgframe.winfo_children()[0].destroy()
            if not user.winfo_children()[0].get()=="":
                if self._main.newUser(user.winfo_children()[0].get(),password.winfo_children()[0].get(),"Admin"):
                    Label(msgframe,text="Account Created").pack()
                else:
                    Label(msgframe,text="Account Name already exists",fg="red").pack()
            else:
                Label(msgframe,text="You must fill the required fields first",fg="red").pack()

        self.header()
        frame = Frame(self.canvas)
        Label(frame,text="New User").grid(row=0, column=0,columnspan=2)
        user = self.makeentry(frame, "Username:")
        user.grid(row=1, column=0)
        password = self.makeentry(frame, "Password:", show="*")
        password.grid(row=1, column=1)
        b=Button(frame,text="Create",command=callback)
        b.config(width=10)
        b.grid(row=2, column=0,columnspan=2)
        frame.pack()
        msgframe = Frame(self.canvas)
        msgframe.pack()

    def header(self):
        user = self._main.curruser
        if user is None:
            self.setView(self.main)

        #frame = Frame(self.canvas)
        header = Menu(self.canvas)
        header.add_command(label="View Cart",command=lambda: self.setView(self.cartView))
        header.add_command(label="Catalogue",command=lambda: self.setView(self.viewCatalogue))
        header.add_command(label="Add Funds",command=lambda: self.setView(self.addFunds))
        header.add_command(label="Check Out",command=lambda: self.setView(self.checkout))
        if isinstance(user,User.Admin):
            adminButtons = Menu(header, tearoff=0)
            adminButtons.add_command(label="View Log",command=lambda: self.setView(self.viewLog))
            adminButtons.add_command(label="New Admin",command=lambda: self.setView(self.newAdmin))
            adminButtons.add_command(label="New Product",command=lambda: self.setView(self.newProduct))
            adminButtons.add_command(label="Delete Product",command=lambda: self.setView(self.deleteProduct))
            adminButtons.add_command(label="Restock",command=lambda: self.setView(self.restock))
            adminButtons.add_command(label="New Category",command=lambda: self.setView(self.newCategory))
            adminButtons.add_command(label="Delete Category",command=lambda: self.setView(self.deleteCategory))
            adminButtons.add_command(label="Delete User",command=lambda: self.setView(self.deleteUser))
            adminButtons.add_command(label="Import",command=lambda: self.setView(self.importData))
            header.add_cascade(label="Admin Commands",menu = adminButtons)
        header.add_command(label="Logout",command=lambda: self.setView(self.main))
        self.canvas.config(menu=header)
        #frame.pack(fill=X)

    def getProductView(self,master,product):
        if isinstance(product,Product.Product):
            frame = LabelFrame(master,text=product.name)
            desc = Label(frame,text=product.description)
            price = Label(frame,text="$"+str(product.price))
            rev = "Reviews\n"
            if len(product.reviews)==0:
                rev+="There are no reviews"
            for r in product.reviews:
                rev += "* "+r+"\n"
            reviews = Label(frame,text=rev)
            deliv = Label(frame,text="Delivery Method: "+product.deliveryMethod)
            desc.pack()
            price.pack()
            reviews.pack()
            deliv.pack()
            return frame

    def viewCatalogue(self):
        def clear():
            for c in frame.winfo_children()[1:]:
                c.destroy()

        def showProduct(event):
            if len(frame.winfo_children())==5:
                frame.winfo_children()[len(frame.winfo_children())-1].destroy()
            if len(event.widget.curselection()):
                name = event.widget.get(event.widget.curselection()[0])
                prod = self._main.getProduct(name)
                if prod is not None:
                    prodframe = self.getProductView(frame,prod)
                    prodframe.pack(side=RIGHT)

        def callback(event):
            name = event.widget['text']
            clear()
            scrollbar = Scrollbar(frame)
            listp = Listbox(frame, yscrollcommand=scrollbar.set)
            listp.bind("<ButtonRelease-1>", showProduct)
            for p in self._main.catalogue.categories.get(self._main.getCategory(name)):
                listp.insert(END,p.name)
            listp.pack(side=LEFT, fill=BOTH)
            scrollbar.pack(side=LEFT,fill=Y)
            scrollbar.config(command=listp.yview)
            Button(frame,text="Add to Cart",command=
            lambda: addProduct(listp.curselection(),listp)).pack(side=LEFT)

        def addProduct(indexes,l):
            if len(frame.winfo_children())==5:
                frame.winfo_children()[len(frame.winfo_children())-1].destroy()
            if len(indexes)>0:
                if self._main.addProduct(l.get(indexes[0])):
                    Label(frame,text="Product Added").pack()
                else:
                    Label(frame,text="Product unavailable or out of stock",fg="red").pack()
            else:
                Label(frame,text="Select a product first",fg="red").pack()

        self.header()
        frame = Frame(self.canvas)
        cat = Frame(frame)
        for c in self._main.catalogue:
            b=Button(cat,text=c.name)
            b.bind("<Button-1>", callback)
            b.pack(side=LEFT)
        cat.pack(fill=BOTH)
        frame.pack(fill=X)

    def addFunds(self):
        def addFunds():
            self._main.addFunds(amount.get())
            l.config(text="Current Balance: "+str(self._main.curruser.getFunds()))
            amount.delete(0,END)

        self.header()
        frame = Frame(self.canvas)
        l = Label(frame,text="Current Balance: "+str(self._main.curruser.getFunds()))
        l.grid(row=0,column=1)
        Label(frame,text="Funds").grid(row=1,column=0)
        amount = Entry(frame,text="0")
        amount.grid(row=1,column=1)
        Button(frame,text="Add",command=addFunds).grid(row=1,column=2)
        frame.pack(fill=BOTH)

    def cartView(self):
        def deleteItem():
            s = mylist.curselection()
            if len(s)>0:
                if self._main.deleteProduct(mylist.get(s[0])):
                    mylist.delete(s[0])
        def clearCart():
            mylist.delete(0,mylist.size())
            self._main.clearCart()

        def viewItem():
            s = mylist.curselection()
            if len(s)>0:
                self.getProductView(frame,self._main.getProduct(mylist.get(s[0])))

        self.header()
        frame = Frame(self.canvas)
        Label(frame,text="Your Cart").pack(fill=X)
        scrollbar = Scrollbar(frame)

        mylist = Listbox(frame, yscrollcommand=scrollbar.set)
        for item in self._main.curruser.getCart():
            mylist.insert(END,item.name)

        mylist.pack(side=LEFT, fill=BOTH)
        scrollbar.pack(side=LEFT,fill=Y)
        scrollbar.config(command=mylist.yview)
        Button(frame,text="Delete Item",command=deleteItem).pack(side=TOP)
        Button(frame,text="Clear Cart",command=clearCart).pack(side=TOP)
        frame.pack()

    def checkout(self):
        def clear():
            for c in frame.winfo_children()[1:]:
                c.destroy()

        def purchase(i):
            clear()
            void = self._main.preformSale(i)
            if len(void)>0:
                Label(frame,text="Unfortunately these item(s) were out of stock",fg="red").pack()
                listframe = LabelFrame(frame,text="Voided Items",padx=5,pady=5,width=50)
                scrollbar = Scrollbar(listframe)
                cartlist = Listbox(listframe,yscrollcommand=scrollbar.set,font="Courier 12",width=30)
                for item in void:
                    cartlist.insert(END,"{:<27s}{:>3d}".format(item.name,void.get(item)))
                cartlist.pack(side=LEFT, fill=BOTH)
                scrollbar.pack(side=LEFT,fill=Y)
                scrollbar.config(command=cartlist.yview)
                listframe.pack()
                Label(frame,text="You were not charged for these",fg="red").pack()
            else:
                Label(frame,text="Purchase Confirmed").pack()
            user.clearCart()

        self.header()
        user = self._main.curruser
        frame = Frame(self.canvas)
        Label(frame,text="Check Out").pack()
        listframe = LabelFrame(frame,text="Your Cart",padx=5,pady=5,width=100)
        scrollbar = Scrollbar(listframe)
        cartlist = Listbox(listframe,yscrollcommand=scrollbar.set,font="Courier 12",width=30)

        items = {}
        total = 0.00
        for i in user.getCart():
            if items.__contains__(i):
                items[i] += 1
            else:
                items[i] = 1
            total += i.price
        if len(items)==0:
            cartlist.insert(END,"No Items In Cart")
        for item in items:
            cartlist.insert(END,"{:<27s}{:>3d}".format(item.name,items.get(item)))

        cartlist.pack(side=LEFT, fill=BOTH)
        scrollbar.pack(side=LEFT,fill=Y)
        scrollbar.config(command=cartlist.yview)
        listframe.pack()
        funds = user.getFunds()
        Label(frame,text="Current Funds: "+str(funds)).pack()
        Label(frame,text="Total: "+str(total)).pack()
        Label(frame,text=" ").pack()
        buttonframe = Frame(frame)
        p = Button(buttonframe,text="Purchase",command=lambda: purchase(items))
        p.pack(side=LEFT)
        if funds<total:
            p.config(stat=DISABLED)
            Label(frame,text="Insufficient Funds",fg="red").pack()
        if len(items)==0:
            p.config(stat=DISABLED)
        Button(buttonframe,text="Return",command=lambda: self.setView(self.viewCatalogue)).pack(side=LEFT)
        buttonframe.pack()
        frame.pack()

    def deleteUser(self):
        def callback():
            if len(name.curselection())>0:
                if len(frame.winfo_children())==5:
                    frame.winfo_children()[len(frame.winfo_children())-1].destroy()
                if self._main.deleteUser(name.get(name.curselection()[0])):
                    Label(frame,text="User Deleted").pack()
                else:
                    Label(frame,text="Unable to Delete User",fg="red").pack()

        self.header()
        frame = Frame(self.canvas)
        Label(frame,text="Select User").pack()
        scrollbar = Scrollbar(frame)
        name = Listbox(frame,yscrollcommand=scrollbar.set)
        for u in self._main.users:
            name.insert(END,u.getUsername())
        name.pack(side=LEFT, fill=BOTH)
        scrollbar.pack(side=LEFT,fill=Y)
        scrollbar.config(command=name.yview)
        Button(frame,text="Delete",command=callback).pack()
        frame.pack()


    def restock(self):
        def callback():
            if len(frame.winfo_children())==4:
                frame.winfo_children()[len(frame.winfo_children())-1].destroy()
            if not amount.winfo_children()[0].get()=="" and len(name.curselection())>0:
                if self._main.restock(name.get(name.curselection()[0]),amount.winfo_children()[0].get()):
                    Label(frame,text="Product Restocked").pack()
                else:
                    Label(frame,text="Failed to Restock Product",fg="red").pack()

        self.header()
        frame = Frame(self.canvas)
        Label(frame,text="Select Product").pack()
        scrollbar = Scrollbar(frame)
        name = Listbox(frame,yscrollcommand=scrollbar.set)
        for c in self._main.catalogue:
            for p in self._main.catalogue.categories.get(c):
                name.insert(END,p.name)
        name.pack(side=LEFT, fill=BOTH)
        scrollbar.pack(side=LEFT,fill=Y)
        scrollbar.config(command=name.yview)
        amount = self.makeentry(frame,"Amount")
        amount.pack()
        Button(frame,text="Restock",command=callback).pack()
        frame.pack()

    def newCategory(self):
        def create():
            if len(frame.winfo_children())==4:
                frame.winfo_children()[len(frame.winfo_children())-1].destroy()
            if self._main.addCategory(name.winfo_children()[0].get(),parent.get()):
                Label(frame,text="Category Created").pack()
            else:
                Label(frame,text="Failed to Create Category",fg="red").pack()

        self.header()
        frame = Frame(self.canvas)
        name = self.makeentry(frame,"Name")
        name.pack()
        Label(frame,text="Parent Category").pack()
        cat = list()
        cat.append("None")
        for c in self._main.catalogue:
            cat.append(c.name)
        parent = Spinbox(frame,value=cat,wrap=True)
        parent.pack()
        Button(frame,text="Create",command=create).pack()
        frame.pack(fill=X)

    def deleteCategory(self):
        def create():
            if len(frame.winfo_children())==3:
                frame.winfo_children()[len(frame.winfo_children())-1].destroy()
            if len(name.curselection())>0:
                if self._main.removeCategory(name.get(name.curselection()[0])):
                    Label(frame,text="Category Deleted").pack()
                else:
                    Label(frame,text="Failed to Delete Category",fg="red").pack()

        self.header()
        frame = Frame(self.canvas)
        Label(frame,text="Select Category").pack()
        scrollbar = Scrollbar(frame)
        name = Listbox(frame,yscrollcommand=scrollbar.set)
        for c in self._main.catalogue:
            name.insert(END,c.name)
        name.pack(side=LEFT, fill=BOTH)
        scrollbar.pack(side=LEFT,fill=Y)
        scrollbar.config(command=name.yview)
        Button(frame,text="Delete",command=create).pack()
        frame.pack()

    def newProduct(self):
        def create():
            if len(frame.winfo_children())==7:
                frame.winfo_children()[len(frame.winfo_children())-1].destroy()
            if self._main.newProduct(name.winfo_children()[0].get(),
                                  desc.winfo_children()[0].get(),
                                  price.winfo_children()[0].get(),
                                  cat.get(),type.get()):
                Label(frame,text="Product Created").pack()
            else:
                Label(frame,text="Failed to Create Product",fg="red").pack()

        self.header()
        frame = Frame(self.canvas)
        name = self.makeentry(frame,"Name")
        name.pack()
        desc = self.makeentry(frame,"Description")
        desc.pack()
        price = self.makeentry(frame,"Price")
        price.pack()
        cats = list()
        for c in self._main.catalogue:
            cats.append(c.name)
        cat = Spinbox(frame,value=cats,wrap=True)
        cat.pack()
        type = Spinbox(frame,value=("Physical","Digital","Subscription"),wrap=True)
        type.pack()
        Button(frame,text="Create",command=create).pack()
        frame.pack()

    def deleteProduct(self):
        def create():
            if len(frame.winfo_children())==3:
                frame.winfo_children()[len(frame.winfo_children())-1].destroy()
            if len(name.curselection())>0:
                if self._main.removeProduct(name.get(name.curselection()[0])):
                    Label(frame,text="Product Deleted").pack()
                else:
                    Label(frame,text="Failed to Delete Product",fg="red").pack()

        self.header()
        frame = Frame(self.canvas)
        Label(frame,text="Select Product").pack()
        scrollbar = Scrollbar(frame)
        name = Listbox(frame,yscrollcommand=scrollbar.set)
        for c in self._main.catalogue:
            for p in self._main.catalogue.categories.get(c):
                name.insert(END,p.name)
        name.pack(side=LEFT, fill=BOTH)
        scrollbar.pack(side=LEFT,fill=Y)
        scrollbar.config(command=name.yview)
        Button(frame,text="Delete",command=create).pack()
        frame.pack()

    def viewLog(self):
        def clear():
            log.delete("1.0",END)
        def event():
            clear()
            log.insert(INSERT,str(self._main.eventLog))
        def product():
            clear()
            log.insert(INSERT,str(self._main.productLog))
        def sales():
            clear()
            log.insert(INSERT,str(self._main.salesLog))
        def user():
            clear()
            log.insert(INSERT,str(self._main.userLog))

        self.header()
        frame = Frame(self.canvas)
        logs = Frame(frame)
        Button(logs,text="Event",command=event).pack(side=LEFT)
        Button(logs,text="Product",command=product).pack(side=LEFT)
        Button(logs,text="Sales",command=sales).pack(side=LEFT)
        Button(logs,text="Users",command=user).pack(side=LEFT)
        logs.pack(fill=X)
        f = Frame(frame)

        log = Text(f, width=100)
        log.pack(fill=X,expand=True)

        f.pack(fill=X)
        frame.pack(fill=BOTH)

    def importData(self):
        def select():
            filename = askopenfilename()
            path.delete(0,END)
            path.insert(0,filename)

        def importFile():
            if len(frame.winfo_children())==4:
                frame.winfo_children()[len(frame.winfo_children())-1].destroy()
            l = self._main.loadData(path.get())
            if len(l)>0:
                f = Frame(frame)
                Label(f,text="Successful Imports").pack()
                scrollbar = Scrollbar(f)
                name = Listbox(f,yscrollcommand=scrollbar.set,width=50)
                for i in l:
                    name.insert(END,i)
                name.pack(side=LEFT, fill=BOTH)
                scrollbar.pack(side=LEFT,fill=Y)
                scrollbar.config(command=name.yview)
                f.pack()
            else:
                Label(frame,text="File Failed to load",fg="red").pack()

        self.header()
        frame = Frame(self.canvas)
        Label(frame,text="Import Data").pack()
        f = Frame(frame)
        Button(f,text="Select",command=select).pack(side=LEFT)
        path = Entry(f,width=100)
        path.insert(0,"C:\\")
        path.pack(side=LEFT)
        f.pack()
        Button(frame,text="Import",command=importFile).pack()
        frame.pack()
