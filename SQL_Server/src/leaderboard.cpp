#include "leaderboard.h"
#include "ui_leaderboard.h"

#include "sql/include/SQL.h"
#include "server/include/Server.h"

#include <atomic>
#include <mutex>
#include <condition_variable>

#include <iostream>

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

void Leaderboard::Update(const Data& notification)
{
    QLabel** player_names[10] =
    {
      &ui->label  , &ui->label_2, &ui->label_3, &ui->label_4, &ui->label_5,
      &ui->label_6, &ui->label_7, &ui->label_8, &ui->label_9, &ui->label_10
    };

    std::string format;
    for(uint8_t i=0; i<(*notification.name).size(); ++i )
    {
        format = (*notification.name)[i] + '/' + (*notification.level)[i] + '/' + (*notification.points)[i];
        auto qstr = QString(format.c_str() );
        (*player_names[i])->setText(qstr);
    }
    format = "";
    for(uint8_t i=(*notification.name).size(); i<10; ++i )
    {
        auto qstr = QString(format.c_str() );
        (*player_names[i])->setText(qstr);
    }
    this->update();
}

void Leaderboard::on_pushButton_clicked()
{
  this->close();
}
