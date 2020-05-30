#ifndef SERVER_H
#define SERVER_H

#include "sql.h"
#include <thread>
#include <atomic>

class Server
{
private:
  std::thread worker;
  class ServerImpl;
  std::unique_ptr<ServerImpl> ServerPimpl;

  void ClientCommunication(void);
public:
  explicit Server(std::shared_ptr<SQL>& sql);
  virtual ~Server();
};

#endif // SERVER_H
