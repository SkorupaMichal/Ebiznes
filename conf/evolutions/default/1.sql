-- !Ups

create table Category(
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    name TEXT NOT NULL
);

create table Product(
    id INTEGER  PRIMARY KEY AUTOINCREMENT,
    name TEXT NOT NULL,
    category_id INTEGER NOT NULL,
    FOREIGN  KEY (category_id) REFERENCES  Category(id)
);

CREATE TABLE Basket(
    id INTEGER  PRIMARY KEY AUTOINCREMENT ,
    description TEXT DEFAULT ""
);

CREATE TABLE BasketHelper(
    id INTEGER  PRIMARY KEY AUTOINCREMENT,
    basket_id INTEGER NOT NULL,
    product_id INTEGER not null,
    FOREIGN KEY (basket_id) REFERENCES Basket(id),
    FOREIGN KEY (product_id) REFERENCES Product(id)
);

CREATE TABLE Comment(
    id INTEGER  PRIMARY KEY AUTOINCREMENT ,
    title TEXT NOT NULL,
    content TEXT NOT NULL,
    product_id INTEGER NOT NULL,
    FOREIGN KEY (product_id) REFERENCES Product(id)
);

CREATE TABLE Deliver(
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    name TEXT NOT NULL,
    description TEXT DEFAULT ""
);

CREATE TABLE Image(
    id INTEGER  PRIMARY KEY AUTOINCREMENT,
    url TEXT NOT NULL,
    description TEXT DEFAULT "",
    product_id INTEGER NOT NULL ,
    FOREIGN KEY (product_id) REFERENCES Product(id)
);

CREATE TABLE Orders(
    id INTEGER PRIMARY KEY AUTOINCREMENT ,
    name TEXT DEFAULT ""
);

CREATE TABLE OrderHelper(
    id INTEGER  PRIMARY KEY AUTOINCREMENT ,
    order_id INTEGER NOT NULL ,
    product_id INTEGER NOT NULL ,
    FOREIGN KEY (order_id) REFERENCES Orders(id),
    FOREIGN KEY (product_id) REFERENCES Product(id)
);

CREATE TABLE Payment(
  id INTEGER  PRIMARY KEY AUTOINCREMENT ,
  name TEXT NOT NULL ,
  description TEXT
);

CREATE TABLE ProductSet(
    id INTEGER  PRIMARY KEY AUTOINCREMENT,
    name TEXT NOT NULL,
    description TEXT DEFAULT ""
);

CREATE TABLE Subcategory(
    id INTEGER  PRIMARY KEY AUTOINCREMENT ,
    name TEXT NOT NULL,
    description TEXT DEFAULT "",
    category_id INTEGER NOT NULL ,
    FOREIGN KEY (category_id) REFERENCES Category(id)
);

-- !Downs

DROP TABLE Category;
DROP TABLE Product;
DROP TABLE Basket;
DROP TABLE Comment;
DROP TABLE Deliver;
DROP TABLE Image;
DROP TABLE Orders;
DROP TABLE Payment;
DROP TABLE ProductSet;
DROP TABLE Subcategory;
DROP TABLE BasketHelper;
DROP TABLE OrderHelper;