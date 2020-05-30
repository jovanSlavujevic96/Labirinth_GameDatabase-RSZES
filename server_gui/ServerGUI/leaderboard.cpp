#include "leaderboard.h"
#include "ui_leaderboard.h"
#include <unistd.h>
#include "server.h"

Leaderboard::Leaderboard(QWidget *parent) :
  QWidget(parent),
  ui(new Ui::Leaderboard)
{
  ui->setupUi(this);
}

Leaderboard::~Leaderboard()
{
  delete ui;
}

void Leaderboard::assign(std::shared_ptr<SQL>& sql)
{
  this->sql = sql;
}

void Leaderboard::LeaderboardOnScreen(void)
{
  QLabel** player_names[10] =
  {
    &ui->label  , &ui->label_2, &ui->label_3, &ui->label_4, &ui->label_5,
    &ui->label_6, &ui->label_7, &ui->label_8, &ui->label_9, &ui->label_10
  };

  while(true)
  {
      auto vecPlayers = sql->getLeaderboard();
      for(uint8_t i=0; i< (uint8_t)vecPlayers.size(); ++i)
      {
        auto qstr = QString(vecPlayers[i].c_str() );
        (*player_names[i])->setText(qstr);
      }
      usleep(1000000);
      this->update();
  }
}

void Leaderboard::on_pushButton_clicked()
{
  this->close();
  std::exit(0);
}
