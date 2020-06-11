/********************************************************************************
** Form generated from reading UI file 'widget.ui'
**
** Created by: Qt User Interface Compiler version 5.15.0
**
** WARNING! All changes made in this file will be lost when recompiling UI file!
********************************************************************************/

#ifndef UI_WIDGET_H
#define UI_WIDGET_H

#include <QtCore/QVariant>
#include <QtWidgets/QApplication>
#include <QtWidgets/QGroupBox>
#include <QtWidgets/QLineEdit>
#include <QtWidgets/QPushButton>
#include <QtWidgets/QWidget>

QT_BEGIN_NAMESPACE

class Ui_Widget
{
public:
    QGroupBox *groupBox;
    QLineEdit *lineEdit;
    QLineEdit *lineEdit_2;
    QPushButton *pushButton;

    void setupUi(QWidget *Widget)
    {
        if (Widget->objectName().isEmpty())
            Widget->setObjectName(QString::fromUtf8("Widget"));
        Widget->resize(400, 300);
        groupBox = new QGroupBox(Widget);
        groupBox->setObjectName(QString::fromUtf8("groupBox"));
        groupBox->setGeometry(QRect(10, 110, 381, 171));
        lineEdit = new QLineEdit(groupBox);
        lineEdit->setObjectName(QString::fromUtf8("lineEdit"));
        lineEdit->setGeometry(QRect(10, 70, 142, 25));
        lineEdit->setTabletTracking(true);
        lineEdit->setDragEnabled(true);
        lineEdit->setReadOnly(false);
        lineEdit->setClearButtonEnabled(true);
        lineEdit_2 = new QLineEdit(groupBox);
        lineEdit_2->setObjectName(QString::fromUtf8("lineEdit_2"));
        lineEdit_2->setGeometry(QRect(10, 110, 142, 25));
        lineEdit_2->setTabletTracking(true);
        lineEdit_2->setEchoMode(QLineEdit::Password);
        lineEdit_2->setDragEnabled(true);
        lineEdit_2->setCursorMoveStyle(Qt::LogicalMoveStyle);
        lineEdit_2->setClearButtonEnabled(true);
        pushButton = new QPushButton(groupBox);
        pushButton->setObjectName(QString::fromUtf8("pushButton"));
        pushButton->setGeometry(QRect(280, 110, 80, 25));
        pushButton->setMouseTracking(true);
        pushButton->setTabletTracking(true);
        pushButton->setAcceptDrops(true);
        pushButton->setAutoFillBackground(true);

        retranslateUi(Widget);

        QMetaObject::connectSlotsByName(Widget);
    } // setupUi

    void retranslateUi(QWidget *Widget)
    {
        Widget->setWindowTitle(QCoreApplication::translate("Widget", "Widget", nullptr));
        groupBox->setTitle(QCoreApplication::translate("Widget", "MySQL sign in", nullptr));
        lineEdit->setPlaceholderText(QCoreApplication::translate("Widget", "username", nullptr));
        lineEdit_2->setPlaceholderText(QCoreApplication::translate("Widget", "password", nullptr));
        pushButton->setText(QCoreApplication::translate("Widget", "sign in", nullptr));
    } // retranslateUi

};

namespace Ui {
    class Widget: public Ui_Widget {};
} // namespace Ui

QT_END_NAMESPACE

#endif // UI_WIDGET_H
