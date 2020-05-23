#include "player.hpp"

player::player(const std::string& email, const std::string& nickname, const std::string& password) :
    email{email}, nickname{nickname}, password{password}
{

}

player::player(const char* email, const char* nickname, const char* password) :
    email{email}, nickname{nickname}, password{password}
{

}

void player::setNickname(const std::string& nickname)
{
    this->nickname = nickname;
}

void player::setNickname(const char* nickname)
{
    this->nickname = nickname;
}

const char* player::getNickname(void) const
{
    return this->nickname.c_str();
}

void player::setPassword(const std::string& password)
{
    this->password = password;
}

void player::setPassword(const char* password)
{
    this->password = password;
}

const char* player::getPassword(void) const
{
    return this->password.c_str();
}

void player::setPoints(const uint16_t points)
{
    this->points = points;
}

uint16_t player::getPoints(void) const
{
    return this->points;
}

void player::setPassedLevel(const uint8_t passed_level)
{
    this->passed_level = passed_level;
}

uint8_t player::getPassedLevel(void) const
{
    return this->passed_level;
}

void player::setSocket(const int64_t socket)
{
    this->socket = socket;
}

int64_t player::getPlayerSocket(void) const
{
    return this->socket;
}

const char* player::getEmail(void) const
{
    return this->email.c_str();
}