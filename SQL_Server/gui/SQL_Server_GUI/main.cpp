#include "widget.h"
#include "leaderboard.h"

#include <QApplication>
#include "../../sql/include/sql.h"
#include "../../server/include/server.h"
#include <QThread>

#include <iostream>

int main(int argc, char **argv)
{
  std::shared_ptr<SQL> sql = std::make_shared<SQL>();
  QApplication a(argc, argv);

  int8_t ret;
  {
    std::unique_ptr<Widget> w = std::make_unique<Widget>();
    w->assign(sql);
    w->show();

    if( (ret = (int8_t) a.exec()) )
      return -1;
  }
  std::unique_ptr<Leaderboard> l = std::make_unique<Leaderboard>();
  l->assign(sql);

  QThread *t1 = QThread::create(&Leaderboard::LeaderboardOnScreen, l.get() );
  t1->start();


  std::unique_ptr<Server> srv = std::make_unique<Server>();
  if(! srv->AssignSQL(sql) )
    return -1;

  if(! srv->RunServer() )
      return -1;

  l->show();

  ret = (int8_t) a.exec();

  t1->wait();
  srv->QuitServer();

  return (int)ret;
}
