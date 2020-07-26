# MazeGame_Database-RSZES

This project is intended for master studies of FTN - Faculty of Technical Sciences. The Subject is  Software development for embedded systems (RSZES). 

In this project has been implemented TCP/IP socket communication between, at first, C++ console application as the server and java android application as the client, afterwards Server has became QT GUI Application. Server handles communication for each opened socket (client) separatly via multi-threading approach. It provides data from SQL database and writes new one into it. SQL database consists of android players informations such as email, name, password and game scores. C++ server has role of medium here between Android client and SQL database. Accessing to SQL was provided by MySQL API. Via TCP/IP, server sends single answers or XML file to the client. XML file is filled with data's from top 10 players on database according to highest score. XML file Generating is provided by TinyXML API. QT GUI provides administrator (developer) to access to SQL database by enetering his username and password on Widget fields and streams leaderboard live (top 10 players). Android application was developed in IntelliJ's Android Studio IDE, written on java and it consists of two main blocks - Game and "Socket handling". Game is actually a simple Maze game, in which player (red square) must go to the blue field as fast as possible to get highest score. There are 5 similar randomly-created levels in which player reaches 500+ points. After game party, clients updates it's score to the SQL via client socket in multi-threads. Android user must be signed in at the beginning of the app so he could communicate with SQL and use some features or he/she can play in "offline mode". In order for a user to log in (sign in), he must first register (sign up). He must enter e-mail, nickname and password at first time, after that he can access to SQL with e-mail or nickname and password. User can get xml file from server and parse it so he can read informations. Whole android application has used many Android SDK API 30 features.
