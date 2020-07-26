# MazeGame_Database-RSZES

This project is intended for master studies of FTN - Faculty of Technical Sciences. The Subject is  Software development for embedded systems (RSZES). 

In this project has been implemented TCP/IP socket communication between, at first, C++ console application as the server and java android application as the client, afterwards Server has became QT GUI Application. Server handles communication for each opened socket (client) separatly via multi-threading approach. It provides data from SQL database and writes new one into it. SQL database consists of android players informations such as email, name, password and game scores. C++ server has role of medium here between Android client and SQL database. Accessing to SQL was provided by MySQL API. Via TCP/IP, server sends single answers or XML file to the client. XML file is filled with data's from top 10 players on database according to highest score. Generating of XML file is provided by TinyXML API.
