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
	pw TEXT,
	username TEXT,
	domain TEXT,
	pwHash TEXT,
	primary key (tripletID),
	foreign key (userID) references Users(userID));
