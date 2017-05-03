# 2016/2017 SEC project Dependable Password Manager #

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

[1] Create a temporary directory

```
> cd ~
> mkdir Project
> cd Project

```

[2] Obtaining source code

```
> git clone  https://github.com/Daniel-Fermoselle/DependablePasswordManager.git

```

[3] Database set-up

```
> cd DependablePasswordManager-SEC_R1/PwServer
Login on MySQL: 
> mysql -u USERNAME -p (where USERNAME is your MySQL id, eg: root)
> source database.sql;
> \q (to quit mysql)

```

[4] Change mysql credentials in the PwServer/metadata/metadata.in if needed

```
In the metadata folder of the PwServer project you'll have a file named metadata.in.
Every line in this file has information to be used by the server specified on that line.
For instance:
	"localhost:5555,PWM_1,root,rootroot,server1"
	(<server uri authority>,<database name>,<mySQL username>,<mySQL password>,<keystore alias>)
	This means that the server running on <localhost:5555> will store data in a database named <PWM_1> 
	using <root> as username and <rootroot> as password for its mySQL credentials and 
	will use <server1> as its alias for the keystore.

We strongly advise you not to change anything besides the name of the database, the mySQL username or password.
If you change the other fields make sure you change the /client_lib/metadata.in (client_lib project) and 
run.command (PwServer project) according to your changes.


```

[5] Install libraries, modules and run the application

```
Run the server script that will launch 5 terminals (4 servers and 1 client)
note that the client will sleep for 25 seconds to wait for the servers' startup.
> ./server.sh install run
Copy and paste the full path to your PwServer project and 
client project (note that it's the client project and not the client_lib).
Insert the path to they keystore and rank you wish (higher number = higher priority).
After 25 seconds the servers should be running and the client will start running.

Alternatively you can run clients on other terminals.
Go to the client project and run:
> ./client.sh install-no-tests "metadata.in ks/client1.jks 100" 
	(for ks/client1.jks keystore and rank 100)


```

```

[5*] Install libraries, modules and run the application with byzantine servers

```
To use byzantine servers you need to change run.command script on lines 9, 10, 11, 22
We have 3 types of byzantine faults:
	-Slow server
	-Always returns a wrong password when retrieving
	-Crash server

For the first add "slow-10" (without "") after localhost:port on one of the lines above.
	This will cause the server to take 10 seconds to send a response you can specify any time you want,
	for example, "slow-5" will cause a delay of 5 seconds
For the second add "fault" after localhost:port on one of the lines above.
	This will make the server change the password that is going to be sent. We always add to the password the word ByzantineFault and wts to 1000.
For the third add "crash" after localhost:port on one of the lines above.
	This will crash the server after it receives a request from a client.


```
 
-------------------------------------------------------------------------------
**END**
