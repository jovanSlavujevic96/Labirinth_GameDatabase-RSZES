#include "server/include/server.h"
#include "sql/include/sql.h"

#include <iostream>

int main(void)
{
    std::shared_ptr<SQL> sql = std::make_unique<SQL>();
    {
        std::string username, password;
        std::cout << "Admin username and password: ";
        std::cin >> username >> password;
        if(!sql->sync(username, password) )
        {
            std::cout << "Wrong admin username or password\n";
            return -1;
        }
    }
    std::unique_ptr<Server> server = std::make_unique<Server>();
    if(!server->AssignSQL(sql) )
    {
        std::cout << "Can't assing SQL to Server's instance\n";
        return -1;
    }
    if(!server->RunServer() )
    {
        std::cout << "Can't run server!\n";
        return -1;
    }

    while(getchar() != 'Q' && getchar() != 'q')
    {
        static int incr = 0;
        std::cout << "Iterration: " << incr++ << " <=> type Q/q to quit!\n";
    }

    server->QuitServer();

    return 0;
}