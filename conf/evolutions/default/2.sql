-- !Ups
INSERT INTO Category (name,description) values ("Samochody","dd"),("AGD","dd"),("RTV","dd");
INSERT INTO User (login,email,password) values ("czarodziej","123@wp.pl","qazwsx");
INSERT INTO Basket ("description",user_id) values ("1",1),("2",1),("3",1);
INSERT INTO Comment (title,content,product_id,user_id) values ("Dobre auto","Samochod spelnia moje oczekiwania",1,1),
                                                      ("OK","OK",2,1),("SUPER","Super pralka",3,1),
                                                      ("Twlewizory :D","OK",4,1);
INSERT INTO Deliver (name,cost,description) values ("Kurier UPS",20,"Kurier najszybciej"),
                                              ("Kurier DPD",10,"Troche szybciej niz UPS"),
                                              ("Poczta polska",5,"Czas oczekiwania 2 tyg");
INSERT INTO Payment (name,description) values ("PayPal","Platnosc internetowa"),
                                              ("Karta kredytowa","Kazdy bank"),
                                              ("Przelwe internetowy","Kazdy bank");
INSERT INTO Subcategory (name,description,category_id) values ("Dostawczy","Samochody dostawcze. najlepsze do firmy",1),
                                                              ("Osobowe", "Samochody dla calej rodziny",1),
                                                              ("Ekspressy do kawy","Zrobi kazdy rodzaj kawy",2),
                                                              ("Telewizory","Telewizory dla kazdego",3);
INSERT INTO Product (name,cost,count,producer,category_id,subcategory_id) values ("BMW",20,10,"BB",1,2),("Porshe",100,10,"Cayman",1,2),("Remington",100000,10,"E.E.E",2,3),("Samsung Galaxy S10",3000,10,"Samsung",3,4);
INSERT INTO ProductBasket(basket_id,product_id) values (1,1),(1,2),(2,3),(3,2);
INSERT INTO Orders (date,cost,deliver_id,user_id,payment_id,basket_id) values ("12:05:2000",34,1,1,1,1);

-- !Downs
DELETE  FROM Category;
DELETE  FROM Product;
DELETE  FROM Basket;
DELETE  FROM Comment;
DELETE  FROM Deliver;
DELETE  FROM Payment;
DELETE  FROM Subcategory;
DELETE  FROM ProductBasket;
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