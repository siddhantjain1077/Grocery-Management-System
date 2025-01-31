#Grocery store management system
#to create database and show databses
# import mysql
import mysql.connector as mys #mys is alias name
import matplotlib.pyplot as plt
import pandas as pd

mycon=mys.connect(host="localhost",user="root",passwd="tiger")
mycursor=mycon.cursor()

# mycursor.execute("CREATE DATABASE Managementsys")
#to create table and show tables in that database
mycon=mys.connect(host="localhost",user="root",passwd="tiger",database="Managementsys")  #to connect the database with the table
if mycon.is_connected():
    print("Successfully connected")
# mycursor=mycon.cursor()
# mycursor.execute("CREATE TABLE Grocery(Item_code integer primary key,Grocery_Item varchar(30),Cost_Price integer,MRP integer, Total_Units integer, Units_sold integer,Income integer)")


def adddata():
        import time
        import mysql
        import mysql.connector as mys
        mycon=mys.connect(host="localhost",user="root",passwd="tiger",database="Managementsys")
        if mycon.is_connected():
            print("Successfully connected")
        mycursor=mycon.cursor()
        c='Y'
        while c in 'Yy':
            print()
            code=int(input("Enter unique Item Code:"))
            print()
            time.sleep(0.5)
            name=input("Enter Grocery item name:")
            price=int(input("Enter item's cost price:Rs."))
            mrp=int(input("Enter MRP:Rs."))
            tot=int(input("Enter total units imported:"))
            sold=int(input("Enter units sold:"))
            if sold>tot:

                print("UNITS SOLD SHOULD BE LESS THAN TOTAL UNITS IMPORTED. KINDLY ADD CORRECT DATA.")
                sold=int(input("Enter units sold:"))
            if mrp>price:
                time.sleep(0.5)       
                income=(mrp-price)*sold
                print("Profit generated:Rs.",income)
            elif mrp<price:
                income=(mrp-price)*sold
                print("Loss:Rs",income)
            elif mrp==price:
                income=0
                print("No profit No loss") 
            st="INSERT INTO Grocery(Item_code,Grocery_Item,Cost_Price,MRP, Total_Units, Units_sold, Income) VALUES({},'{}',{},{},{},{},{})".format(code,name,price,mrp,tot,sold,income)
            mycursor.execute(st)
            time.sleep(2)
            print()
            print("%100s"%"RECORD ADDED SUCCESSFULLY!!!")
            mycon.commit()
            c=input("%100s"%"Do you want to add more records(Yy,Nn):")


def fetchdata():
    import mysql
    import mysql.connector as mys
    try:
        mycon=mys.connect(host="localhost",user="root",passwd="tiger",database="Managementsys")
        if mycon.is_connected():
            print("Successfully connected")
        mycursor=mycon.cursor()
        mycursor.execute('SELECT * FROM Grocery')
        myrecords=mycursor.fetchall()
        nrec=mycursor.rowcount
        print("%100s"%"Total no. of records:",nrec)
        print()
        print("---------------------------------------------------------------------------------------------------------------------------------------------------")
        print("%50s"%"Item_code","%15s"%"Grocery_Item","%10s"%"Cost Price","%10s"%"MRP","%15s"%"Total_units","%15s"%"Units_Sold","%10s"%"Income")
        print("====================================================================================================================================================")
        for row in myrecords:
            print("\n%50s"%row[0],"%15s"%row[1],"%10s"%row[2],"%10s"%row[3],"%15s"%row[4],"%15s"%row[5],"%10s"%row[6])
        print("===================================================================================================================================================")
        print()

    except:
        print("Error: Unable to fetch data")

def updatedata():
    import time
    import mysql
    import mysql.connector as mys
    try:
        mycon=mys.connect(host="localhost",user="root",passwd="tiger",database="Managementsys")
        if mycon.is_connected():
            print("Successfully connected")
        mycursor=mycon.cursor()
        mycursor.execute('SELECT * FROM Grocery')
        myrecords=mycursor.fetchall()
        print()
        print("%100s"%"RECORDS BEFORE UPDATION.")
        print()
        time.sleep(1)
        print("---------------------------------------------------------------------------------------------------------------------------------------------------")
        print("%50s"%"Item_code","%15s"%"Grocery_Item","%10s"%"Cost Price","%10s"%"MRP","%15s"%"Total_units","%15s"%"Units_Sold","%10s"%"Income")
        print("====================================================================================================================================================")
        for row in myrecords:
            print("\n%50s"%row[0],"%15s"%row[1],"%10s"%row[2],"%10s"%row[3],"%15s"%row[4],"%15s"%row[5],"%10s"%row[6])
        print("===================================================================================================================================================")
        print()

        code=int(input("%100s"%"Enter the Item_code whose record is to be updated:"))
        mycursor.execute('SELECT * FROM Grocery')
        a=[]
        for col in mycursor:
            a.append(col[0])
        if code not in a:
            print("Record does not exist!!!")
        else:
            print()
            c=input("Do you want to update the cost price of the item or MRP or total untis sold?(Yy/Nn):")
            if c=='Y' or c=='y':
                time.sleep(1)
                print("PLEASE REFER TO ORIGINAL VALUES GIVEN IN ABOVE TABLE IF YOU DON'T WANT TO UPDATE.")
                print()
                price=int(input("Enter new cost price:Rs."))
                mrp=int(input("Enter new MRP:Rs."))
                tot=int(input("Enter total units imported:"))
                sold=int(input("How many items are sold?:"))
                if sold>tot:
                    print("UNITS SOLD SHOULD BE LESS THAN TOTAL UNITS IMPORTED. KINDLY ADD CORRECT DATA.")
                    sold=int(input("How many items are sold?:"))
                if mrp>price:
                    income=(mrp-price)*sold
                    print("Profit generated:Rs.",income)
                elif mrp<price:
                    income=(mrp-price)*sold
                    print("Loss incurred:Rs",income)
                elif mrp==price:
                        income=0
                        print("No profit No loss")


                st="UPDATE Grocery SET Cost_Price={} WHERE Item_code={}".format(price,code)
                x="UPDATE Grocery SET Income={} WHERE Item_code={}".format(income,code)
                y="UPDATE Grocery SET MRP={} WHERE Item_code={}".format(mrp,code)
                z="UPDATE Grocery SET Units_sold={} WHERE Item_code={}".format(sold,code)
                u="UPDATE Grocery SET Total_Units={} WHERE Item_code={}".format(tot,code)
                mycursor.execute(st)
                mycursor.execute(x)
                mycursor.execute(y)
                mycursor.execute(z)
                mycursor.execute(u)
                mycon.commit()
            print()
            print()
            print("%100s"%"Record updated successfully!!")
            print()
            print("%100s"%"RECORDS AFTER UPDATION")
            mycursor.execute('SELECT * FROM Grocery')
            myrecords=mycursor.fetchall()
            print()
            print("------------------------------------------------------------------------------------------------------------------------------------------------")
            print("%50s"%"Item_code","%15s"%"Grocery_Item","%10s"%"Cost Price","%10s"%"MRP","%15s"%"Total_units","%15s"%"Units_Sold","%10s"%"Income")
            print("=================================================================================================================================================")
            for row in myrecords:
                print("\n%50s"%row[0],"%15s"%row[1],"%10s"%row[2],"%10s"%row[3],"%15s"%row[4],"%15s"%row[5],"%10s"%row[6])
            print("==================================================================================================================================================")
    except Exception as e:
        print(e)



def deldata():

    import time
    import mysql
    import mysql.connector as mys
    try:
        mycon=mys.connect(host="localhost",user="root",passwd="tiger",database="Managementsys")
        if mycon.is_connected():
            print("Successfully connected")
        mycursor=mycon.cursor()
        mycursor.execute('SELECT * FROM Grocery')
        myrecords=mycursor.fetchall()
        print()
        print("------------------------------------------------------------------------------------------------------------------------------------------------------")
        print("%50s"%"Item_code","%15s"%"Grocery_Item","%10s"%"Cost Price","%10s"%"MRP","%15s"%"Total_units","%10s"%"Units_Sold","%10s"%"Income")
        print("========================================================================================================================================================")
        for row in myrecords:
            print("\n%50s"%row[0],"%15s"%row[1],"%10s"%row[2],"%10s"%row[3],"%15s"%row[4],"%10s"%row[5],"%10s"%row[6])
        print("======================================================================================================================================================")


        c='Y'
        a=[]
        while c in 'Yy':
            print()
            code=int(input("Enter the item_code referring to the record you want to delete:"))
            mycursor.execute("SELECT * from Grocery")
            for col in mycursor:
                a.append(col[0])
            if code not in a:
                print()
                time.sleep(0.5)
                print("Sorry, record does not exist!!")

            else:
                st="DELETE FROM Grocery WHERE Item_code={}".format(code)
                mycursor.execute(st)
                time.sleep(0.5)
                print("%100s"%"RECORD DELETED SUCCESSFULLY!!")
                mycon.commit()
                print()
            c=input("%100s"%"Do you want to delete any more records?(Yy/Nn)")
    except:
        print("Unable to fetch data.")
def costMRP():
    import matplotlib.pyplot as plt
    import pandas as pd
    import time
    import mysql
    import mysql.connector as mys
    try:
        mycon=mys.connect(host="localhost",user="root",passwd="tiger",database="Managementsys")
        if mycon.is_connected():
            print("Successfully connected")
        mycursor=mycon.cursor()
        print('1.Bar Graph')
        print('2.Line Chart')
        ch=int(input('How do you wish to display :'))
        if ch==1:
                            co="select Cost_price from Grocery"
                            mycursor.execute(co)
                            cos=mycursor.fetchall()
        
                            mr="select MRP from Grocery"
                            mycursor.execute(mr)
                            mrpp=mycursor.fetchall()
                            x="select count(*) from Grocery"
                            mycursor.execute(x)
                            col=mycursor.fetchall()
                            for i in col:
                                        x=i[0]
                            z=[]
                            q=[]
                            for j in range(0,x):
                                            for y in (cos[j]):

                                                            z.append(y)
                            for m in range(0,x):
                                            for u in (mrpp[m]):
                                                            q.append(u)
                            df=pd.DataFrame({'Cost Price':z,'MRP':q})
                            print(df)
                            plt.bar(q,z,width=5,color='y')
                            plt.xlabel('MRP')
                            plt.ylabel('Cost Price')
                            plt.title('Cost VS MRP')
                            plt.show()
                            
        elif ch==2:

                            co="select Cost_price from Grocery"
                            mycursor.execute(co)
                            cos=mycursor.fetchall()
        
                            mr="select MRP from Grocery"
                            mycursor.execute(mr)
                            mrpp=mycursor.fetchall()
                            x="select count(*) from Grocery"
                            mycursor.execute(x)
                            col=mycursor.fetchall()
                            for i in col:
                                        x=i[0]
                            z=[]
                            q=[]
                            for j in range(0,x):
                                        for y in (cos[j]):
                                                    z.append(y)
                            for m in range(0,x):
                                        for u in (mrpp[m]):
                                                        q.append(u)
                            df=pd.DataFrame({'Cost Price':z,'MRP':q})
                            print(df)
                            plt.plot(q,z,marker='*',markersize=10)
                            plt.xlabel('MRP')
                            plt.ylabel('Cost Price')
                            plt.title('Cost VS MRP')
                            plt.show()
    except Exception as e:

        print(e)
def costTot():
    import matplotlib.pyplot as plt
    import pandas as pd
    import time
    import mysql
    import mysql.connector as mys
    try:
        mycon=mys.connect(host="localhost",user="root",passwd="tiger",database="Managementsys")
        if mycon.is_connected():
            print("Successfully connected")
        mycursor=mycon.cursor()
        print('1.Bar Graph')
        print('2.Line Chart')
        ch=int(input('How do you wish to display :'))
        if ch==1:
                            ci="select Cost_price from Grocery"
                            mycursor.execute(ci)
                            cis=mycursor.fetchall()
        
                            ms="select Total_Units from Grocery"
                            mycursor.execute(ms)
                            mrsp=mycursor.fetchall()
                            x="select count(*) from Grocery"
                            mycursor.execute(x)
                            col=mycursor.fetchall()
                            for i in col:
                                        x=i[0]
                            a=[]
                            b=[]
                            for j in range(0,x):
                                            for y in (cis[j]):
                                                            a.append(y)
                            for m in range(0,x):
                                            for u in (mrsp[m]):
                                                            b.append(u)
                            df=pd.DataFrame({'Cost Price':a,'Total Units Sold':b})
                            print(df)
                            plt.bar(b,a,width=5,color='b')
                            plt.xlabel('Total Units Sold')
                            plt.ylabel('Cost Price')

                            plt.title('Cost VS Total Units Sold')
                            plt.show()
                            
        elif ch==2:

                            ci="select Cost_price from Grocery"
                            mycursor.execute(ci)
                            cis=mycursor.fetchall()
        
                            ms="select MRP from Grocery"
                            mycursor.execute(ms)
                            mrsp=mycursor.fetchall()
                            x="select count(*) from Grocery"
                            mycursor.execute(x)
                            col=mycursor.fetchall()
                            for i in col:
                                        x=i[0]
                            a=[]
                            b=[]
                            for j in range(0,x):
                                        for y in (cis[j]):
                                                    a.append(y)
                            for m in range(0,x):
                                        for u in (mrsp[m]):
                                                        b.append(u)
                            df=pd.DataFrame({'Cost Price':a,'Total Units Sold':b})
                            print(df)
                            plt.plot(b,a,marker='*',markersize=10)
                            plt.xlabel('Total Units Sold')
                            plt.ylabel('Cost Price')
                            plt.title('Cost VS Total Units Sold')
                            plt.show()
    except Exception as e:
        print(e)

def gis():
    import matplotlib.pyplot as plt
    import pandas as pd
    import time
    import mysql
    import mysql.connector as mys
    try:
        mycon=mys.connect(host="localhost",user="root",passwd="tiger",database="Managementsys")
        if mycon.is_connected():
            print("Successfully connected")
        mycursor=mycon.cursor()
        print('1.Bar Graph')
        print('2.Line Chart')
        ch=int(input('How do you wish to display :'))
        if ch==1:
                            to="select Total_Units from Grocery"
                            mycursor.execute(to)
                            tos=mycursor.fetchall()
        
                            ti="select Units_Sold from Grocery"
                            mycursor.execute(ti)
                            trpp=mycursor.fetchall()
                            x="select count(*) from Grocery"
                            mycursor.execute(x)
                            col=mycursor.fetchall()
                            for i in col:
                                        x=i[0]
                            e=[]
                            f=[]
                            for j in range(0,x):
                                            for y in (tos[j]):
                                                            e.append(y)
                            for m in range(0,x):
                                            for u in (trpp[m]):
                                                            f.append(u)
                            df=pd.DataFrame({'Total Units Imported':e,'Total Units Sold':f})
                            print(df)
                            plt.bar(e,f,width=5,color='g')
                            plt.xlabel('Total Units Imported')
                            plt.ylabel('Total Units Sold')
                            plt.title('Total Units Imported Vs Total Units Sold')
                            plt.show()
                            
        elif ch==2:

                            to="select Total_Units from Grocery"
                            mycursor.execute(to)
                            tos=mycursor.fetchall()
        

                            ti="select MRP from Grocery"
                            mycursor.execute(ti)
                            trpp=mycursor.fetchall()
                            x="select count(*) from Grocery"
                            mycursor.execute(x)
                            col=mycursor.fetchall()
                            for i in col:
                                        x=i[0]
                            e=[]
                            f=[]
                            for j in range(0,x):
                                        for y in (tos[j]):
                                                    e.append(y)
                            for m in range(0,x):
                                        for u in (trpp[m]):
                                                     f.append(u)
                            df=pd.DataFrame({'Total Units Imported':e,'Total Units Sold':f})
                            print(df)
                            plt.plot(e,f,marker='*',markersize=10,color='y')
                            plt.xlabel('Total Units Imported')
                            plt.ylabel('Total Units Sold')
                            plt.title('Total Units Imported Vs Total Units Sold')
                            plt.show()
    except Exception as e:
        print(e)

# print("%100s"%"WELCOME TO RYAN INTERNATIONAL SCHOOL")   
# print("%95s"%"EXCELLENCE IN EDUCATION.")
# print("%95s"%"AND ALL ROUND DEVELOPEMENT")
# print("\nFOLLOWING ARE THE OPTIONS FOR YOU TO PERFORM ON GROCERY MANAGEMENT SYSTEM.")

x=0
while x==0:
        print()
        print("1. Add Record")
        print("2. Update Record")
        print("3. Delete Record")
        print("4. Display Record")
        print("5. Comparison Charts") 
        print("6. Exit")
        choice=int(input("Enter your choice:"))

        if choice==1:
            adddata()
        elif choice==2:
            updatedata()
        elif choice==3:
            deldata()
        elif choice==4:
            fetchdata()
        elif choice==5:
            print("1. Cost Vs MRP")
            print("2. Cost Vs Total Goods Sold") 
            print("3. Total Good Imported Vs Total Goods Sold")
            s=int(input('Enter your choice :'))
            if s==1:
                costMRP()
            elif s==2:
                costTot()
            elif s==3:
                gis()
            else:
                print('Enter a valid choice')
        elif choice==6:
            print("Exiting...")
            quit()
        else:
            print("Enter correct choice.")
menu()
