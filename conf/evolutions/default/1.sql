-- !Ups

create table Category(
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    name TEXT NOT NULL,
    description TEXT NOT NULL
);

create table Product(
    id INTEGER  PRIMARY KEY AUTOINCREMENT,
    name TEXT NOT NULL,
    cost INTEGER NOT NULL,
    count   INTEGER NOT NULL,
    producer TEXT NOT NULL,
    subcategory_id INTEGER NOT NULL,
    FOREIGN  KEY (subcategory_id) REFERENCES  Subcategory(id)
);
CREATE TABLE User(
    id INTEGER  PRIMARY KEY AUTOINCREMENT,
    login TEXT NOT NULL,
    email TEXT NOT NULL,
    password TEXT NOT NULL
);
CREATE TABLE Basket(
    id INTEGER  PRIMARY KEY AUTOINCREMENT ,
    description TEXT DEFAULT "",
    user_id     INTEGER NOT NULL,
    FOREIGN KEY (user_id) REFERENCES User(id)
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
    user_id    INTEGER NOT NULL,
    FOREIGN KEY (product_id) REFERENCES Product(id),
    FOREIGN KEY (user_id)    REFERENCES User(id)
);

CREATE TABLE Deliver(
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    name TEXT NOT NULL,
    cost INTEGER NOT NULL,
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
    date TEXT DATE,
    cost INTEGER NOT NULL,
    deliver_id INTEGER NOT NULL,
    user_id INTEGER NOT NULL,
    payment_id INTEGER NOT NULL,
    basket_id  INTEGER NOT NULL,
    FOREIGN KEY (user_id) REFERENCES User(id),
    FOREIGN KEY (payment_id) REFERENCES Deliver(id),
    FOREIGN KEY (deliver_id) REFERENCES Payment(id),
    FOREIGN KEY (basket_id) REFERENCES Basket(id)
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
DROP TABLE User;
DROP TABLE Subcategory;
DROP TABLE BasketHelper;
DROP TABLE OrderHelper;