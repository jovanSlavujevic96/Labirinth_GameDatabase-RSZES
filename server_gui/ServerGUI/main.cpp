#include "widget.h"
#include "leaderboard.h"

#include <QApplication>
#include "sql.h"
#include "server.h"
#include <QThread>

int main(int argc, char **argv)
{
  std::shared_ptr<SQL> sql = std::make_shared<SQL>();
  QApplication a(argc, argv);

  int8_t ret;
  {
    std::unique_ptr<Widget> w = std::make_unique<Widget>();
    w->assign(sql);
    w->show();

    ret = (int8_t) a.exec();
    if(ret)
      return -1;
  }
  std::unique_ptr<Leaderboard> l = std::make_unique<Leaderboard>();
  l->assign(sql);

  QThread *t1 = QThread::create(&Leaderboard::LeaderboardOnScreen, l.get() );
  t1->start();

  std::unique_ptr<Server> srv = std::make_unique<Server>(sql);

  l->show();

  return a.exec();
}
