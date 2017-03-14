# 2016/2017 SEConf project Dependable Password Manager #

Group SEC 03 - Campus Alameda

Daniel Fermoselle   78207 daniel.fermosele@gmail.com

João Marçal         78471 joao.marcal12@gmail.com

Tiago Rodrigues     78692 tiagomsr4s@gmail.com

Repositório:
[Daniel-Fermoselle/DependablePasswordManager](https://github.com/Daniel-Fermoselle/DependablePasswordManager)

-------------------------------------------------------------------------------

## Set-up Instructions


### Environment

[0] Boot operating system

Mac OSx versão 10.11.6 arch: x86_64 (version used during project development)  
Java version "1.8.0_111" (version used during project development)  
MySQL version 5.7.16 (version used during project development)  
Apache Maven version 3.3.9 (version used during project development)  

[1] Create a temporary directory

```
cd ~
mkdir Project
cd Project
```

[2] Obtaining source code

```
git clone  https://github.com/Daniel-Fermoselle/DependablePasswordManager.git
git checkout ????
```

[3] Database set-up

```
????
cd PwServer
Login on MySQL: mysql -uroot -p (to enter with root)
source database.sql;
\q (para sair do mysql)

```

[4] Install libraries and modules

```

./server.sh path ????
cd ../client
./client.sh clean
./client.sh install

```
 
-------------------------------------------------------------------------------
**FIM**