
insert into Game("GameName") values("Game 1");
insert into Accounts("PlayerName") values("Player 1");
insert into Participation("AID","GID") values(1,1),(2,1),(6,1);
insert into Turn("TurnNumber","PID") values(0,1),(0,2),(0,3),(1,1),(1,2);

insert into Location("TID","Room","Location","Step") values
(1,0,"[1,3,0]",0),
(1,0,"[1,4,0]",1),
(1,0,"[2,3,0]",2),
(1,0,"[3,3,0]",3),
(2,0,"[10,3,0]",0),
(2,0,"[10,4,0]",1),
(2,0,"[11,4,0]",2),
(3,0,"[21,3,0]",0),
(4,0,"[1,29,0]",0),
(4,0,"[1,30,0]",1),
(4,0,"[1,31,0]",2),
(5,0,"[6,3,0]",0),
(5,0,"[5,3,0]",1);


--Returns every MaxTurn for every account in the game
--select Participation.AID as AID, TID, max(TurnNumber) as MaxTurn from Turn join Participation on (Turn.PID=Participation.PID) where Participation.GID=1 group by AID;

--ReturnsLocation of highest TurnNumber and of that the highest Step from each PID



select AID, Location, Colour from (
        select AID, x.TID, Location, max(Step), Colour from Location x inner join (
                select AID, TID, max(TurnNumber), Colour 
                from Turn join Participation on (Turn.PID=Participation.PID) 
                where Participation.GID=1 
                group by AID
        ) y on (y.TID = x.TID) group by x.TID
) z join Turn on (Turn.TID=z.TID);

V2 with Player Name
select AID, Room, Location, Colour, PlayerName from (
        select AID, x.TID, Room, Location, max(Step), Colour, PlayerName from Location x inner join (
            select Participation.AID as AID, TID, max(TurnNumber), Colour, PlayerName 
            from Turn join Participation on (Turn.PID=Participation.PID) join Accounts on (Participation.AID=Accounts.AID)
            where Participation.GID=GID
            group by Participation.AID
        ) y on (y.TID = x.TID) group by x.TID
) z join Turn on (Turn.TID=z.TID);




select TID, AID,Roll1,Roll2 from Turn join Participation on (Turn.PID=Participation.PID) where TurnNumber=(
        select max(TurnNumber) as MaxTurn from Turn join Participation on (Turn.PID=Participation.PID)
        where Participation.GID=1
);

insert into PlayerCards("PID","CID") values 
(1,1),
(1,4),
(1,5),
(2,6),
(2,3),
(2,2),
(3,28),
(3,27),
(3,26);


insert into HiddenCards("GID","CID") values 
(1,14),
(1,16),
(1,15);

insert into CommunityCards("GID","CID") values 
(1,12);


delete from Location where TID in (select Location.TID from Participation join Turn on (Participation.PID=Turn.PID) join Location on (Turn.TID=Location.TID) where GID=1);
delete from Question where TID in (select TID from Participation join Turn on (Participation.PID=Turn.PID) where GID=1);
delete from PlayerCards where PID in (select PlayerCards.PID,CID from Participation join PlayerCards on (Participation.PID=PlayerCards.PID) where GID=1); 
delete from CommunityCards where GID =1; 
delete from Hiddencards where GID =1; 
delete from Turn where TID in (select TID from Participation join Turn on (Participation.PID=Turn.PID) where GID=1);
delete from Participation where GID=1;
delete from Game where GID=1;
        


delete from Location where TID in (select Location.TID from Participation join Turn on (Participation.PID=Turn.PID) join Location on (Turn.TID=Location.TID) where GID=16);
delete from Question where TID in (select TID from Participation join Turn on (Participation.PID=Turn.PID) where GID=16);
delete from PlayerCards where PID in (select PlayerCards.PID from Participation join PlayerCards on (Participation.PID=PlayerCards.PID) where GID=16);
delete from CommunityCards where GID =16;
delete from HiddenCards where GID =16;
delete from Turn where TID in (select TID from Participation join Turn on (Participation.PID=Turn.PID) where GID=16);
delete from Participation where GID=16;
delete from Game where GID=16;


select Participation.AID as AnswererAID, CardSuspect, CardWeapon, CardRoom, Response from Question 
join Participation on (Participation.PID=Question.Answerer) 
where QID in (
        select QID from Question 
        join Turn on (Turn.TID=Question.TID)
        where Turn.PID=41
) and Answerer!=41;

select Participation.AID as AnswererAID, CardSuspect, CardWeapon, CardRoom, Response from Question 
join Participation on (Participation.PID=Question.Answerer) 
join Turn on (Turn.TID=Question.TID)
where Answerer!=334 and Turn.PID!=334 and Response != 0 and Participation.GID=63;

update Accounts set Points = 300, GamesPlayed=0, WrongGuesses=0, Losses=0, Wins=0; 

select count(case when AID < 5 then 1 end) as BotNumber, GameSize, Game.GID
from Game join Participation on (Participation.GID = Game.GID) group by Game.GID;

select GID from Game
where GameSize = 3 and GID in (
        select GID from (
                select count(case when AID < 5 then 1 end) as BotNumber, GameSize, Game.GID
                from Game join Participation on (Participation.GID = Game.GID) group by Game.GID
        ) x
        where x.BotNumber = 2 
);
            


