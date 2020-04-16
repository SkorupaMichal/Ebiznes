-- !Ups

INSERT INTO Category (name) values ("Samochody"),("AGD"),("RTV");
INSERT INTO Product (name,category_id) values ("BMW",1),("Porshe",1),("Remington",2),("Samsung Galaxy S10",3);
INSERT INTO Basket ("description") values ("1"),("2"),("3");
INSERT INTO Comment (title,content,product_id) values ("Dobre auto","Samochod spelnia moje oczekiwania",1),
                                                      ("OK","OK",2),("SUPER","Super pralka",3),
                                                      ("Twlewizory :D","OK",4);
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
-- !Downs
DELETE  FROM Category;
DELETE  FROM Product;
DELETE  FROM Basket;
DELETE  FROM Comment;
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