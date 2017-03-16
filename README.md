# 2016/2017 SEConf project Dependable Password Manager #

Group SEC 03 - Campus Alameda

Daniel Fermoselle   78207 daniel.fermosele@gmail.com

João Marçal         78471 joao.marcal12@gmail.com

Tiago Rodrigues     78692 tiagomsr4s@gmail.com

Repository:
[Daniel-Fermoselle/DependablePasswordManager](https://github.com/Daniel-Fermoselle/DependablePasswordManager)

-------------------------------------------------------------------------------

## Set-up Instructions


### Environment

[0] Boot operating system

Mac OSx version 10.11.6 arch: x86_64 (version used during project development);
Java version "1.8.0_111" (version used during project development);
MySQL version 5.7.16 (version used during project development)
	-Also download this https://dev.mysql.com/downloads/connector/j/ and follow the three steps in 2.2(For Mac OS) of this link https://www.ntu.edu.sg/home/ehchua/programming/java/JDBC_Basic.htmlconnector/j/ ;
Apache Maven version 3.3.9 (version used during project development, we recommend you to install this via brew);  
Tomcat 8.5.1 (version used during project development, we recommend you to install this via brew);  

[1] Create a temporary directory

```
> cd ~
> mkdir Project
> cd Project
```

[2] Obtaining source code

```
> git clone  https://github.com/Daniel-Fermoselle/DependablePasswordManager.git
> git checkout ????
```

[3] Database set-up

```
> cd PwServer
Login on MySQL: 
> mysql -u USERNAME -p (where USERNAME is your MySQL id, eg: root)
> source database.sql;
> \q (to quit mysql)

```

[4] Change mysql credentials in the database access file

```
> cd src/main/java/pt/sec/a03/server/database/
Open Database.java in a text editor and change the fields to your MySQL id and password:
	- MYSQL_ID (root by default)
	- MYSQL_PASSWORD  (rootroot by default)
> cd ../service
Open VaultService.java in a text editor and change the fields to the keystore absolute path:
	- serverKeyStorePath (the keystore is located in the Project/PwServer/ks directory)

Return to PwServer

```

[5] Install libraries, modules and run the application

```
Return to PwServer, use the following command if you are still in the service directory
> cd ../../../../../../../../ 

Now, copy the path to your webapp folder of tomcat
If you installed tomcat with brew you can copy to your command line the following command and you'll know where this folder is
> brew ls tomcat
What you are looking for is something like this /usr/local/Cellar/tomcat/8.5.11/libexec/webapps/

> ./server.sh path (eg: ./server.sh /usr/local/Cellar/tomcat/8.5.11/libexec/webapps/)
> cd ../client
> ./client.sh clean
> ./client.sh install
> mvn exec:java

Important thing to note here when you run the command:
> ./client.sh install
The server must be executing so you can pass the integrity tests, i.e. you must execute the following command before:
>./server.sh path


```
 
-------------------------------------------------------------------------------
**FIM**