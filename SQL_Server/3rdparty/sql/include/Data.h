#ifndef DATA_H
#define DATA_H

#include <vector>
#include <string>

struct Data
{
    std::vector<std::string>* name;
    std::vector<std::string>* level;
    std::vector<std::string>* points;
    const char* FILENAME;
};

#endif //DATA_H