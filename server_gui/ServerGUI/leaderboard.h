#ifndef LEADERBOARD_H
#define LEADERBOARD_H

#include <QWidget>
#include "sql.h"
#include "server.h"

namespace Ui {
  class Leaderboard;
}

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
  std::shared_ptr<SQL> sql;
  std::shared_ptr<Server> server;
};

#endif // LEADERBOARD_H
