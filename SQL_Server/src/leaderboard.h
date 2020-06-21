#ifndef LEADERBOARD_H
#define LEADERBOARD_H

#include <QWidget>
#include <thread>
#include <memory>

QT_BEGIN_NAMESPACE
namespace Ui {
  class Leaderboard;
}
QT_END_NAMESPACE

class SQL;
class Server;

class Leaderboard : public QWidget
{
  Q_OBJECT

public:
  explicit Leaderboard(QWidget *parent = nullptr);
  virtual ~Leaderboard();

  void assign(std::shared_ptr<SQL>& sql);
  void LeaderboardOnScreen(void);

private slots:
  void on_pushButton_clicked();

private:
  Ui::Leaderboard *ui;
  std::shared_ptr<Server> server;
  std::shared_ptr<SQL> sql;

  void interruptTimer(void);
  std::thread timer;
};

#endif // LEADERBOARD_H
