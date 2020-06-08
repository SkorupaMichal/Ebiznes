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
CREATE TABLE role(
    id   INTEGER NOT NULL PRIMARY KEY,
    name VARCHAR
);

CREATE TABLE user(
    id         VARCHAR    NOT NULL PRIMARY KEY,
    first_name VARCHAR,
    last_name  VARCHAR,
    email      VARCHAR,
    role_id    INTEGER     NOT NULL,
    avatar_url VARCHAR,
    CONSTRAINT auth_user_role_id_fk FOREIGN KEY (role_id) REFERENCES role(id)
);

CREATE TABLE login_info(
    id           INTEGER NOT NULL PRIMARY KEY,
    provider_id  VARCHAR,
    provider_key VARCHAR
);

CREATE TABLE user_login_info(
    user_id       VARCHAR   NOT NULL,
    login_info_id INTEGER NOT NULL,
    CONSTRAINT auth_user_login_info_user_id_fk FOREIGN KEY (user_id) REFERENCES user(id),
    CONSTRAINT auth_user_login_info_login_info_id_fk FOREIGN KEY (login_info_id) REFERENCES login_info(id)
);

CREATE TABLE oauth2_info (
    id            INTEGER NOT NULL PRIMARY KEY,
    access_token  VARCHAR   NOT NULL,
    token_type    VARCHAR,
    expires_in    INTEGER,
    refresh_token VARCHAR,
    login_info_id INTEGER    NOT NULL,
    CONSTRAINT auth_oauth2_info_login_info_id_fk FOREIGN KEY (login_info_id) REFERENCES login_info(id)
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
CREATE TABLE Address(
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    city VARCHAR NOT NULL,
    street VARCHAR NOT NULL,
    zip_code VARCHAR NOT NULL
);
CREATE TABLE Orders(
    id INTEGER PRIMARY KEY AUTOINCREMENT ,
    date TEXT DATE,
    cost INTEGER NOT NULL,
    deliver_id INTEGER NOT NULL,
    user_id VARCHAR NOT NULL,
    payment_id INTEGER NOT NULL,
    basket_id  INTEGER NOT NULL,
    address_id INTEGER NOT NULL,
    FOREIGN KEY (user_id) REFERENCES User(id) ON DELETE CASCADE,
    FOREIGN KEY (payment_id) REFERENCES Deliver(id),
    FOREIGN KEY (deliver_id) REFERENCES Payment(id),
    FOREIGN KEY (basket_id) REFERENCES Basket(id) ON DELETE CASCADE,
    FOREIGN KEY (address_id) REFERENCES Addresses(id)
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
DROP TABLE Address;
DROP TABLE Orders;
DROP TABLE Payment;
DROP TABLE User;
DROP TABLE LoginInfo;
DROP TABLE UserLoginInfo;
DROP TABLE OAuth2Info;
DROP TABLE Subcategory;
DROP TABLE ProductBasket;
DROP TABLE OrderHelper;