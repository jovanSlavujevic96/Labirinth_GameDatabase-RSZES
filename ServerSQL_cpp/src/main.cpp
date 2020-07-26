#include "widget.h"
#include "leaderboard.h"

#include <QApplication>
#include "SQL.h"
#include "Server.h"

#include <iostream>

int main(int argc, char **argv)
{
  std::unique_ptr<SQL> sql = std::make_unique<SQL>();

  QApplication a(argc, argv);

  std::unique_ptr<Leaderboard> l = std::make_unique<Leaderboard>();
  sql->attach(l.get() );

  std::unique_ptr<Server> srv = std::make_unique<Server>();
  sql->attach(srv.get() );
  int8_t ret;
  {
    std::unique_ptr<Widget> w = std::make_unique<Widget>();
    w->assign(sql.get() );
    w->show();

    if( (ret = (int8_t) a.exec()) )
      return -1;
  }


  if(! srv->AssignSQL(std::move(sql)) )
    return -1;

  if(! srv->RunServer() )
      return -1;

  l->show();

  ret = static_cast<int8_t>(a.exec() );

  srv->QuitServer();

  return (int)ret;
}
