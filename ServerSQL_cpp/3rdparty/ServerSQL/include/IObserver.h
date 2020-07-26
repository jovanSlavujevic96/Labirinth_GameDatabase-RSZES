#ifndef IOBSERVER_H
#define IOBSERVER_H

#include "Data.h"

//OBSERVER PATTERN INTERFACE

class IObserver
{
public:
    explicit IObserver() = default;
    virtual ~IObserver() = default;

    //OBSERVER VIRTUAL METHOD
    //DATA WRAPPER STRUCTURE
    virtual void Update(const Data& notification) = 0;
};  

#endif //IOBSERVER_H