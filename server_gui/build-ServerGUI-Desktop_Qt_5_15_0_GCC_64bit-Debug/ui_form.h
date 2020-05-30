/********************************************************************************
** Form generated from reading UI file 'form.ui'
**
** Created by: Qt User Interface Compiler version 5.15.0
**
** WARNING! All changes made in this file will be lost when recompiling UI file!
********************************************************************************/

#ifndef UI_FORM_H
#define UI_FORM_H

#include <QtCore/QVariant>
#include <QtWidgets/QApplication>
#include <QtWidgets/QWidget>

QT_BEGIN_NAMESPACE

class Ui_Leaderboard
{
public:

    void setupUi(QWidget *Leaderboard)
    {
        if (Leaderboard->objectName().isEmpty())
            Leaderboard->setObjectName(QString::fromUtf8("Leaderboard"));
        Leaderboard->resize(556, 397);

        retranslateUi(Leaderboard);

        QMetaObject::connectSlotsByName(Leaderboard);
    } // setupUi

    void retranslateUi(QWidget *Leaderboard)
    {
        Leaderboard->setWindowTitle(QCoreApplication::translate("Leaderboard", "Form", nullptr));
    } // retranslateUi

};

namespace Ui {
    class Leaderboard: public Ui_Leaderboard {};
} // namespace Ui

QT_END_NAMESPACE

#endif // UI_FORM_H
