#ifndef MYSQLCPP_INCLUDE_SERVER_HPP_
#define MYSQLCPP_INCLUDE_SERVER_HPP_

#include <sql.hpp>
#include <vector>

class Server
{
private:
    class ServerImpl;
    std::unique_ptr<ServerImpl> ServerPimpl;
public: 
    explicit Server(std::unique_ptr<SQL>& sql);
    virtual ~Server();

    void ClientCommunication(void);
};

#endif //MYSQLCPP_INCLUDE_SERVER_HPP_
