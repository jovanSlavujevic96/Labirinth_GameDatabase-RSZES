#include "widget.h"
#include "ui_widget.h"

#include "sql/include/sql.h"

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
  Widget::SQL_admin_login();
}

void Widget::on_lineEdit_2_returnPressed()
{
  Widget::SQL_admin_login();
}

void Widget::on_lineEdit_returnPressed()
{
  Widget::SQL_admin_login();
}

void Widget::SQL_admin_login()
{
  const auto username = this->ui->lineEdit->text().toStdString(),
                    password = this->ui->lineEdit_2->text().toStdString();
  const bool info = sql->sync(username, password);
  if(info)
  {
    this->close();
  }
}
