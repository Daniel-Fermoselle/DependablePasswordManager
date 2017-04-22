create database if not exists experiments;
 
use experiments;
 
drop table if exists Vault;
drop table if exists Users;
drop table if exists Bonrrs;

create table Users (
   userID bigint AUTO_INCREMENT,
   publicKey TEXT,
   nonce bigint,
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

create table Bonrrs (
	bonrr VARCHAR(255),
	wts bigint,
	signature TEXT,
	username TEXT,
	domain TEXT,
	pw TEXT,
	pwHash TEXT,
	primary key (bonrr, wts));

create database if not exists PWM_1;
 
use PWM_1;
 
drop table if exists Vault;
drop table if exists Users;
drop table if exists Bonrrs;

create table Users (
   userID bigint AUTO_INCREMENT,
   publicKey TEXT,
   nonce bigint,
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

create table Bonrrs (
	bonrr VARCHAR(255),
	wts bigint,
	signature TEXT,
	username TEXT,
	domain TEXT,
	pw TEXT,
	pwHash TEXT,
	primary key (bonrr, wts));

create database if not exists PWM_2;
 
use PWM_2;
 
drop table if exists Vault;
drop table if exists Users;
drop table if exists Bonrrs;

create table Users (
   userID bigint AUTO_INCREMENT,
   publicKey TEXT,
   nonce bigint,
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

create table Bonrrs (
	bonrr VARCHAR(255),
	wts bigint,
	signature TEXT,
	username TEXT,
	domain TEXT,
	pw TEXT,
	pwHash TEXT,
	primary key (bonrr, wts));

create database if not exists PWM_3;
 
use PWM_3;
 
drop table if exists Vault;
drop table if exists Users;
drop table if exists Bonrrs;

create table Users (
   userID bigint AUTO_INCREMENT,
   publicKey TEXT,
   nonce bigint,
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

create table Bonrrs (
	bonrr VARCHAR(255),
	wts bigint,
	signature TEXT,
	username TEXT,
	domain TEXT,
	pw TEXT,
	pwHash TEXT,
	primary key (bonrr, wts));

create database if not exists PWM_4;
 
use PWM_4;
 
drop table if exists Vault;
drop table if exists Users;
drop table if exists Bonrrs;

create table Users (
   userID bigint AUTO_INCREMENT,
   publicKey TEXT,
   nonce bigint,
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

create table Bonrrs (
	bonrr VARCHAR(255),
	wts bigint,
	signature TEXT,
	username TEXT,
	domain TEXT,
	pw TEXT,
	pwHash TEXT,
	primary key (bonrr, wts));
