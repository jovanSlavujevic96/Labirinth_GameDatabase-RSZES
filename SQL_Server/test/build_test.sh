g++ test.cpp -o test -pthread -L../server -lserver -L../sql -lsql --std=c++17 `mysql_config --cflags --libs`
