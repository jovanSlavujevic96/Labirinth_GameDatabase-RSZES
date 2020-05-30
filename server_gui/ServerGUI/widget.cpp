#include "widget.h"
#include "ui_widget.h"

Widget::Widget(QWidget *parent)
  : QWidget(parent)
  , ui(new Ui::Widget)
{
    ui->setupUi(this);
}

Widget::~Widget()
{
    delete ui;
}

void Widget::assign(std::shared_ptr<SQL>& sql)
{
    this->sql = sql;
}

void Widget::on_pushButton_clicked()
{
    const std::string username = this->ui->lineEdit->text().toStdString(),
                      password = this->ui->lineEdit_2->text().toStdString();
    const bool info = sql->sync(username.c_str(), password.c_str() );
    if(info)
    {
      this->close();
    }
}
