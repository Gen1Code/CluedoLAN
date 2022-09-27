/*
drop table if exists Accounts;
create table Accounts(
AID integer primary key autoincrement,
SessionID varchar(32), 
PlayerName varchar(40),
Email varchar(60) unique,
Password varchar(128),
Salt varchar(32),
Wins integer default 0,
Losses integer default 0,
WrongGuesses integer default 0,
GamesPlayed integer default 0, 
Points integer default 300
);

insert into Accounts("AID","PlayerName") values(0,"Bot");
insert into Accounts("AID","PlayerName") values(1,"Bot1");
insert into Accounts("AID","PlayerName") values(2,"Bot2");
insert into Accounts("AID","PlayerName") values(3,"Bot3");
insert into Accounts("AID","PlayerName") values(4,"Bot4 ");
insert into Accounts("AID","PlayerName") values(5,"Bot5 ");
*/

drop table if exists Cards ;
create table Cards(
CID integer primary key autoincrement,
CardName varchar(20)
);

insert into Cards("CID","CardName") values (0,"");
insert into Cards("CardName") values ("Col. Mustard"),--1
("Prof. Violet"),
("Dr. Green"),
("Mrs Peacock"),
("Miss Scarlet"),
("Mrs White"),--6
("M Dubois"),
("Ayane Kuroki"),
("Miss Carrot"),
("Sr Lapis"),--10
("Knife"), --11
("Candlestick"),
("Revolver"),
("Rope"),
("Lead Pipe"),
("Wrench"),--16
("Flower Pot"),
("Poison"),
("Dumbbell"),
("Shuriken"),--20
("Hall"),--21
("Lounge"),
("Dining Room"),
("Kitchen"),
("Ballroom"),
("Conservatory"),
("Billiard Room"),
("Library"),
("Study"),--29
("Bedroom"),
("Balcony"),
("Fitness Room"),
("Play Room");--33

drop table if exists Game;
create table Game(
GID integer primary key autoincrement,
GameName varchar(12),
GameSize integer,
GameType varchar(7),
GameOrder varchar(60),
GameState varchar(10) default '',
PointsDistributed varchar(60) default ''
);

drop table if exists Participation;
create table Participation(
PID integer primary key autoincrement,
AID integer,
GID integer,
Colour varchar(7),
StartingLocation varchar(9),
foreign key(AID) references Accounts,
foreign key(GID) references Game,
constraint Unique_Participant unique (AID,GID)
);

drop table if exists PlayerCards;
create table PlayerCards(
PID integer,
CID integer,
foreign key (PID) references Participation,
foreign key (CID) references Cards,
primary key (PID,CID)
);

drop table if exists HiddenCards;
create table HiddenCards(
GID integer,
CID integer,
foreign key (GID) references Game,
foreign key (CID) references Cards,
primary key (GID,CID)
);

drop table if exists CommunityCards;
create table CommunityCards(
GID integer,
CID integer,
foreign key (GID) references Game,
foreign key (CID) references Cards,
primary key (GID,CID)
);

drop table if exists Location;
create table Location(
TID integer,
Room integer default 0,
Location varchar(9),
Step integer,
constraint NoDoubleSteps unique(TID,Step),
foreign key(TID) references Turn,
foreign key(Room) references Cards
);

drop table if exists Turn;
create table Turn(
TID integer primary key,
TurnNumber integer,
PID integer,
Roll1 integer default 0, 
Roll2 integer default 0,
constraint OneTurn unique(TurnNumber,PID),
foreign key (PID) references Participation
);

drop table if exists Question;
create table Question(
QID integer primary key,
TID integer,
CardSuspect integer,
CardWeapon integer,
CardRoom integer,
Answerer integer,
Response integer default 0, --If question is not answered keep 0
constraint OneAnswer unique(TID,Answerer),
foreign key (CardSuspect,CardWeapon,CardRoom,Response) references Cards,
foreign key (Answerer) references Participation,
foreign key (TID) references Turn,
check (Response = CardSuspect or Response = CardWeapon or Response = CardRoom or Response = 0 or Response = -1 or Response =-2)
);
