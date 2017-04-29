create database if not exists experiments;
 
use experiments;

drop table if exists Bonrrs; 
drop table if exists Users;

create table Users (
   publicKey VARCHAR(500),
   nonce bigint,
   primary key (publicKey));

create table Bonrrs (
	bonrr VARCHAR(500),
	wts bigint,
	rank bigint,
	signature TEXT,
	username TEXT,
	domain TEXT,
	pw TEXT,
	pwHash TEXT,
	primary key (bonrr, wts),
	foreign key (bonrr) references Users(publicKey));

create database if not exists PWM_1;
 
use PWM_1;
 
drop table if exists Bonrrs; 
drop table if exists Users;

create table Users (
   publicKey VARCHAR(500),
   nonce bigint,
   primary key (publicKey));

create table Bonrrs (
	bonrr VARCHAR(500),
	wts bigint,
	rank bigint,
	signature TEXT,
	username TEXT,
	domain TEXT,
	pw TEXT,
	pwHash TEXT,
	primary key (bonrr, wts),
	foreign key (bonrr) references Users(publicKey));

create database if not exists PWM_2;
 
use PWM_2;
 
drop table if exists Bonrrs; 
drop table if exists Users;

create table Users (
   publicKey VARCHAR(500),
   nonce bigint,
   primary key (publicKey));

create table Bonrrs (
	bonrr VARCHAR(500),
	wts bigint,
	rank bigint,
	signature TEXT,
	username TEXT,
	domain TEXT,
	pw TEXT,
	pwHash TEXT,
	primary key (bonrr, wts),
	foreign key (bonrr) references Users(publicKey));

create database if not exists PWM_3;
 
use PWM_3;
 
drop table if exists Bonrrs; 
drop table if exists Users;

create table Users (
   publicKey VARCHAR(500),
   nonce bigint,
   primary key (publicKey));

create table Bonrrs (
	bonrr VARCHAR(500),
	wts bigint,
	rank bigint,
	signature TEXT,
	username TEXT,
	domain TEXT,
	pw TEXT,
	pwHash TEXT,
	primary key (bonrr, wts),
	foreign key (bonrr) references Users(publicKey));

create database if not exists PWM_4;
 
use PWM_4;
 
drop table if exists Bonrrs; 
drop table if exists Users;

create table Users (
   publicKey VARCHAR(500),
   nonce bigint,
   primary key (publicKey));

create table Bonrrs (
	bonrr VARCHAR(500),
	wts bigint,
	rank bigint,
	signature TEXT,
	username TEXT,
	domain TEXT,
	pw TEXT,
	pwHash TEXT,
	primary key (bonrr, wts),
	foreign key (bonrr) references Users(publicKey));
