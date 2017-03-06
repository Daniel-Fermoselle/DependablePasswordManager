create database if not exists experiments;
 
use experiments;
 
drop table if exists Vault;
drop table if exists Users;

create table Users (
   userID bigint AUTO_INCREMENT,
   publicKey TEXT,
   primary key (userID));

create table Vault (
	tripletID bigint AUTO_INCREMENT,
	userID bigint,
	pw varchar(255),
	username varchar(255),
	domain varchar(255),
	primary key (tripletID),
	foreign key (userID) references Users(userID)
	);

insert into Users(publicKey) values ('123');
insert into Users(publicKey) values ('sjicasjisaasisajciasjicajsjicasjisaasisajciasjicajsjicasjisaasisajciasjicajsjicasjisaasisajciasjicajsjicasjisaasisajciasjicajsjicasjisaasisajciasjicajsjicasjisaasisajciasjicajsjicasjisaasisajciasjicajsjicasjisaasisajciasjicajsjicasjisaasisajciasjicajsjicasjisaasisajciasjicajsjicasjisaasisajciasjicajsjicasjisaasisajciasjicajsjicasjisaasisajciasjicajsjicasjisaasisajciasjicajsjicasjisaasisajciasjicaj');

insert into Vault(userID, pw, username, domain) values ( 1, 'pas', 'username', 'tecnico');
insert into Vault(userID, pw, username, domain) values ( 2, 'pas', 'username', 'tecnico');