#ifndef SERVER_H
#define SERVER_H

#include <memory>

class SQL;

class Server
{
private:
    class ServerImpl;
    std::unique_ptr<ServerImpl> srv_pimpl;
public:
    explicit Server();
    virtual ~Server();

    bool RunServer(void);
    bool QuitServer(void);
    bool AssignSQL(std::shared_ptr<SQL>& sql);
};

#endif // SERVER_H
