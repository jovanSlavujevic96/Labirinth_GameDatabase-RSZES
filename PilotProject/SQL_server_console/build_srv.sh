g++ test.cpp -o test -pthread -L../../SQL_Server/3rdparty/server/lib -lserver -L../../SQL_Server/3rdparty/sql/lib -lsql --std=c++17 `mysql_config --cflags --libs` -I ../../SQL_Server/3rdparty/
