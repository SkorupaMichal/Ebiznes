-- !Ups

INSERT INTO Category (name) values ("Samochody"),("AGD"),("RTV");
INSERT INTO User (login,email,password) values ("czarodziej","123@wp.pl","qazwsx");
INSERT INTO Basket ("description",user_id) values ("1",1),("2",1),("3",1);
INSERT INTO Comment (title,content,product_id,user_id) values ("Dobre auto","Samochod spelnia moje oczekiwania",1,1),
                                                      ("OK","OK",2,1),("SUPER","Super pralka",3,1),
                                                      ("Twlewizory :D","OK",4,1);
INSERT INTO Deliver (name,description) values ("Kurier UPS","Kurier najszybciej"),
                                              ("Kurier DPD","Troche szybciej niz UPS"),
                                              ("Poczta polska","Czas oczekiwania 2 tyg");
INSERT INTO Payment (name,description) values ("PayPal","Platnosc internetowa"),
                                              ("Karta kredytowa","Kazdy bank"),
                                              ("Przelwe internetowy","Kazdy bank");
INSERT INTO Subcategory (name,description,category_id) values ("Dostawczy","Samochody dostawcze. najlepsze do firmy",1),
                                                              ("Osobowe", "Samochody dla calej rodziny",1),
                                                              ("Ekspressy do kawy","Zrobi kazdy rodzaj kawy",2),
                                                              ("Telewizory","Telewizory dla kazdego",3);
INSERT INTO Product (name,count,producer,subcategory_id) values ("BMW",10,"BB",2),("Porshe",10,"Cayman",2),("Remington",10,"E.E.E",3),("Samsung Galaxy S10",10,"Samsung",4);

INSERT INTO Orders (date,cost,deliver_id,user_id,payment_id) values ("12:05:2000",34,1,1,1);

-- !Downs
DELETE  FROM Category;
DELETE  FROM Product;
DELETE  FROM Basket;
DELETE  FROM Comment;
DELETE  FROM Deliver;
DELETE  FROM Payment;
DELETE  FROM Subcategory;
DELETE  FROM User;

/*
DELETE  FROM Category WHERE id=1;
DELETE  FROM Category WHERE id=2;
DELETE  FROM Category WHERE id=3;
DELETE  FROM Product WHERE id=1;
DELETE  FROM Product WHERE id=2;
DELETE  FROM Product WHERE id=3;
DELETE  FROM Product WHERE id=4;
DELETE  FROM Basket WHERE id=1;
DELETE  FROM Basket WHERE id=1;
DELETE  FROM Basket WHERE id=1;
DELETE  FROM Comment WHERE id=1;
DELETE  FROM Comment WHERE id=1;
DELETE  FROM Comment WHERE id=1;
DELETE  FROM Comment WHERE id=1;
*/