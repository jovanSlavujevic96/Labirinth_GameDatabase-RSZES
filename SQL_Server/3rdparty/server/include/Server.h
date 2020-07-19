#ifndef SERVER_H
#define SERVER_H

#include <memory>
#include "../../sql/include/IObserver.h"

class SQL;

class Server : public IObserver
{
private:
    class ServerImpl;
    class Socket;
    std::unique_ptr<ServerImpl> srv_pimpl;
public:
    Server();
    ~Server();

    bool RunServer(void);
    bool QuitServer(void);
    bool AssignSQL(std::unique_ptr<SQL> sql);

    //OVERRIDEN METHODS
    void Update(const Data& notification) override;
};

#endif // SERVER_H
