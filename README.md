This program allows people to play Cluedo across a LAN, one computer runs this program (use IntelliJ, I have no clue how to make it work for other IDEs) and it is available across the LAN, hosted on port 8080,
For hosting computer '127.0.0.1:8080' connects to the server.  
For other computers to connect, open command prompt and type in 'ipconfig /all' in the hosting computer to find the correct ipv4 address
(it should be under either Wireless LAN adapter Wifi or Ethernet "" ""),
Other computers enter ip:8080 as the url (for most web browsers, some are special look it up its quite easy) (dont use old internet explorer please, some JS stuff doesn't work).  
One computer can have several accounts open at once, just use different web browsers, e.g. Chrome and Microsoft Edge, might want to have monitors seperating browsers though if you dont wanna see each others cards.
This makes this a multiplayer Cluedo implementation (obviously you can play solo vs the computer)

![image](https://user-images.githubusercontent.com/72555054/158430117-794fefd5-ebcf-4dda-8e1b-a05112af2531.png)

How to Play   
Space bar - Roll the Dice  
S - Use Secret Passageway  
Arrow Keys - Move Around  
Top Cards - Community Cards, every player can see these  
Bottom Cards - Player Cards, no one else can see these  
Coloured circles with arrow - represent who is currently & going to play   
Tick Card - maybe try clicking somewhere  
Action List (the blob of text) - well... read it  


There is an original variation of Cluedo which has a 1st Floor, have fun.  
If you can beat 6 Bots without cheating you're insane (I swear they don't cheat, they're just logic beasts).  
![image](https://user-images.githubusercontent.com/72555054/158430243-8aab2409-1c14-48ad-9d42-6fefa7525f1b.png)


Passwords are hashed so no worries about that.

Most refreshes of pages won't disturb the play, but not exhaustively tested  
Any bugs found then please notify me, I'd like to fix them myself   :)     
(work out how to notify me yourself ;)


Card Pictures are either taken from google images, which have Creative Commons License or generated for personal use, the main board image is taken from someone else (sorry I forgot source).  
The code for this project was more about showing complexity and being creative than about consistent concise code, most of it is commented with easy stuff being left uncommented, sorry about that.  

Possible Project Not Running Fixes:  
Mark sqlite-jdbc & json-simple as Libraries  
Check dependencies (Maven problems sometimes)  
Check Program Language level should be 16  
Use OpenJDK 16  

![image](https://user-images.githubusercontent.com/72555054/158429068-c2e91f70-748d-49a4-97c8-82458ea35b72.png)
