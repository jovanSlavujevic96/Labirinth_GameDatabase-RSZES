#ifndef LEADERBOARD_H
#define LEADERBOARD_H

#include <QWidget>
#include <memory>

QT_BEGIN_NAMESPACE
namespace Ui {
  class Leaderboard;
}
QT_END_NAMESPACE

#include "IObserver.h"

class Leaderboard :
    public QWidget,
    public IObserver
{
  Q_OBJECT

public:
  explicit Leaderboard(QWidget *parent = nullptr);
  virtual ~Leaderboard();

  void Update(const Data& notification) override;

private slots:
  void on_pushButton_clicked();

private:
  Ui::Leaderboard *ui;
};

#endif // LEADERBOARD_H
