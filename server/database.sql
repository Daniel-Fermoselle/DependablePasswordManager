create database if not exists experiments;
 
use experiments;
 
drop table if exists Triplet;
create table Triplet (
   username char(255),
   password char(255),
   domain char(255),
   primary key (username, password, domain));

insert into Triplet values ('nasTyMSR', 'bravo', 'google');