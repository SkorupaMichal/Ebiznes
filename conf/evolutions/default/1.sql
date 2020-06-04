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
    category_id INTEGER NOT NULL,
    subcategory_id INTEGER NOT NULL,
    FOREIGN KEY  (category_id) REFERENCES Category(id) ON DELETE CASCADE ,
    FOREIGN  KEY (subcategory_id) REFERENCES  Subcategory(id) ON DELETE CASCADE
);
CREATE TABLE Role(
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    name TEXT NOT NULL
);
CREATE TABLE User(
    id TEXT NOT NULL PRIMARY KEY ,
    firstName TEXT,
    lastName TEXT,
    email TEXT,
    avatar_url TEXT,
    role_id INTEGER NOT NULL,
    FOREIGN KEY (role_id) REFERENCES Role(id)
);
CREATE TABLE LoginInfo(
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    provider_id TEXT,
    provider_key TEXT
);
CREATE TABLE UserLoginInfo(
    user_id TEXT NOT NULL,
    login_info_id INTEGER NOT NULL,
    FOREIGN KEY (user_id) REFERENCES User(id),
    FOREIGN KEY (login_info_id) REFERENCES LoginInfo(id)
);

CREATE TABLE OAuth2Info(
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    access_token TEXT NOT NULL,
    toke_type TEXT,
    expires_in INTEGER,
    refresh_token TEXT,
    login_info_id INTEGER NOT NULL,
    FOREIGN KEY (login_info_id) REFERENCES LoginInfo(id)
);
CREATE TABLE Basket(
    id INTEGER  PRIMARY KEY AUTOINCREMENT ,
    description TEXT DEFAULT "",
    user_id     VARCHAR NOT NULL,
    FOREIGN KEY (user_id) REFERENCES User(id) ON DELETE CASCADE
);

CREATE TABLE ProductBasket(
    id INTEGER  PRIMARY KEY AUTOINCREMENT,
    basket_id INTEGER NOT NULL,
    product_id INTEGER not null,
    FOREIGN KEY (basket_id) REFERENCES Basket(id) ON DELETE CASCADE,
    FOREIGN KEY (product_id) REFERENCES Product(id)
);

CREATE TABLE Comment(
    id INTEGER  PRIMARY KEY AUTOINCREMENT ,
    title TEXT NOT NULL,
    content TEXT NOT NULL,
    product_id INTEGER NOT NULL,
    user_id    VARCHAR NOT NULL,
    FOREIGN KEY (product_id) REFERENCES Product(id) ON DELETE CASCADE,
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
    FOREIGN KEY (product_id) REFERENCES Product(id) ON DELETE CASCADE
);

CREATE TABLE Orders(
    id INTEGER PRIMARY KEY AUTOINCREMENT ,
    date TEXT DATE,
    cost INTEGER NOT NULL,
    deliver_id INTEGER NOT NULL,
    user_id VARCHAR NOT NULL,
    payment_id INTEGER NOT NULL,
    basket_id  INTEGER NOT NULL,
    FOREIGN KEY (user_id) REFERENCES User(id) ON DELETE CASCADE,
    FOREIGN KEY (payment_id) REFERENCES Deliver(id),
    FOREIGN KEY (deliver_id) REFERENCES Payment(id),
    FOREIGN KEY (basket_id) REFERENCES Basket(id) ON DELETE CASCADE
);

CREATE TABLE OrderHelper(
    id INTEGER  PRIMARY KEY AUTOINCREMENT ,
    order_id INTEGER NOT NULL ,
    product_id INTEGER NOT NULL ,
    FOREIGN KEY (order_id) REFERENCES Orders(id) ON DELETE CASCADE,
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
    FOREIGN KEY (category_id) REFERENCES Category(id) ON DELETE CASCADE
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
DROP TABLE LoginInfo;
DROP TABLE UserLoginInfo;
DROP TABLE OAuth2Info;
DROP TABLE Subcategory;
DROP TABLE ProductBasket;
DROP TABLE OrderHelper;