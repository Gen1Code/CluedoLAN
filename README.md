Don't copy this Project, no selling or any kind of illegal stuff.  
Some code has been taken from other people including Stack Overflow and Github, 99.9% of it has been adapted to fit my needs.  
Card Pictures are either taken from google images, most of which have Creative Commons Liciense or generated for personal use, the main board image is taken from someone else (sorry).  
The code for this project was more about showing complexity and being creative than about consistent concise code, most of it is commented with easy stuff being left uncommented, sorry about that.  

This program allows people to play Cluedo across a LAN, one computer runs this program (use IntelliJ, I have no clue how to make it work for other IDEs) and it is available across the LAN ~~unless Firewalls~~, hosted on port 8080,
For hosting computer '127.0.0.1:8080' connects to the server.  
For other computers to connect, open command prompt and type in 'ipconfig /all' in the hosting computer to find the correct ipv4 address
(it should be under either Wireless LAN adapter Wifi or Ethernet "" ""),
Other computers enter ip:8080 as the url (for most web browsers, some are special) and dont use old internet explorer please, some JS stuff doesnt work.  
One computer can have several accounts open at once, just use different web browsers, e.g. Chrome and Microsoft Edge, might want to have monitiors seperating browsers though if you dont wanna see each others cards.

![image](https://user-images.githubusercontent.com/72555054/158430117-794fefd5-ebcf-4dda-8e1b-a05112af2531.png)

How to Play   
Space bar - Roll the Dice  
S - Use Secret Passageway  
Arrow Keys - Move Around

There is an original variation of Cluedo have fun.  
If you can beat 6 Bots without cheating you're insane.
![image](https://user-images.githubusercontent.com/72555054/158430243-8aab2409-1c14-48ad-9d42-6fefa7525f1b.png)


Passwords are hashed so no worries about that.

Most refreshes of pages won't disturb the play, but not fully tested  
Any bugs found then please notify me, I'd like to fix them myself   :)     
(work out how to notify me yourself ;)


Possible Project Not Running Fixes:  
Mark sqlite-jdbc & json-simple as Libraries  
Check dependencies (Maven problems sometimes)  
Check Program Language level should be 16  
Use OpenJDK 16  

![image](https://user-images.githubusercontent.com/72555054/158429068-c2e91f70-748d-49a4-97c8-82458ea35b72.png)
