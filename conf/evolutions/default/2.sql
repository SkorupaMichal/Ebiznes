-- !Ups

INSERT INTO Category (name) values ("Samochody"),("AGD"),("RTV");
INSERT INTO Product (name,category_id) values ("BMW",1),("Porshe",1),("Pralki",2),("Twlewizory",3);
INSERT INTO Basket ("description") values ("1"),("2"),("3");
INSERT INTO Comment (title,content,product_id) values ("Dobre auto","Samochod spelnia moje oczekiwania",1),
("OK","OK",2),("SUPER","Super pralka",3),("Twlewizory :D","OK",4);

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