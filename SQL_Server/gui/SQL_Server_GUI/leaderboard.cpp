#include "leaderboard.h"
#include "ui_leaderboard.h"
#include <unistd.h>
#include "../../server/include/server.h"

#include <atomic>
#include <mutex>
#include <condition_variable>

#include <iostream>

#define TimerCounterLimit (0xf4240/100) //0xf4240 -> million

namespace Leaderboard_MThandler
{
  static std::mutex mutex;
  static std::condition_variable cv;
  static std::atomic<bool> timerOn = true;
  static volatile bool start_counting = false;
}

Leaderboard::Leaderboard(QWidget *parent) :
  QWidget(parent),
  ui(new Ui::Leaderboard)
{
  ui->setupUi(this);
  timer = std::thread(&Leaderboard::interruptTimer, this);
}

Leaderboard::~Leaderboard()
{
  delete ui;
  timer.detach();
}

void Leaderboard::assign(std::shared_ptr<SQL>& sql)
{
  this->sql = sql;
}

void Leaderboard::interruptTimer(void)
{
    uint32_t timerIncr = 0;
    while(true == static_cast<bool>(Leaderboard_MThandler::timerOn) )
    {
        if(!Leaderboard_MThandler::start_counting)
        {
          std::unique_lock<std::mutex> lk(Leaderboard_MThandler::mutex);
          Leaderboard_MThandler::cv.wait(lk, []{return (true == Leaderboard_MThandler::start_counting); });
        }
        (void)usleep(10);
        if( (++timerIncr) == (0xf4240/100) )
        {

            Leaderboard_MThandler::start_counting = false;
            Leaderboard_MThandler::cv.notify_one();
            timerIncr = 0;
        }
    }
    std::cout << "interruptTimer :: exit\n";
    Leaderboard_MThandler::start_counting = false;
    Leaderboard_MThandler::cv.notify_one();
}

void Leaderboard::LeaderboardOnScreen(void)
{
  QLabel** player_names[10] =
  {
    &ui->label  , &ui->label_2, &ui->label_3, &ui->label_4, &ui->label_5,
    &ui->label_6, &ui->label_7, &ui->label_8, &ui->label_9, &ui->label_10
  };

  while(true == static_cast<bool>(Leaderboard_MThandler::timerOn) )
  {
      std::unique_lock<std::mutex> lk(Leaderboard_MThandler::mutex);
      Leaderboard_MThandler::cv.wait(lk, []{ return(false == Leaderboard_MThandler::start_counting);} );
      if(sql.get() != nullptr)
      {
        auto vecPlayers = sql->getLeaderboard();
        for(uint8_t i=0; i< (uint8_t)vecPlayers.size(); ++i)
        {
          auto qstr = QString(vecPlayers[i].c_str() );
          (*player_names[i])->setText(qstr);
        }
      }
      this->update();
      Leaderboard_MThandler::start_counting = true;
      Leaderboard_MThandler::cv.notify_one();
  }
  std::cout << "LeaderboardOnScreen :: exit\n";
  Leaderboard_MThandler::start_counting = true;
  Leaderboard_MThandler::cv.notify_one();
}

void Leaderboard::on_pushButton_clicked()
{
  this->close();
  Leaderboard_MThandler::timerOn = false;
  Leaderboard_MThandler::cv.notify_all();
}
