#include "player.h"

Player::Player(const std::string& email, const std::string& nickname, const std::string& password) :
    email{email}, nickname{nickname}, password{password}
{

}

Player::Player(const char* email, const char* nickname, const char* password) :
    email{email}, nickname{nickname}, password{password}
{

}

void Player::setNickname(const std::string& nickname)
{
    this->nickname = nickname;
}

void Player::setNickname(const char* nickname)
{
    this->nickname = nickname;
}

void Player::setPassword(const std::string& password)
{
    this->password = password;
}

void Player::setPassword(const char* password)
{
    this->password = password;
}

void Player::setPoints(const uint16_t points)
{
    this->points = points;
}

void Player::setLevel(const uint8_t passed_level)
{
    this->passed_level = passed_level;
}

const char* Player::getEmail(void) const
{
    return this->email.c_str();
}

const char* Player::getNickname(void) const
{
    return this->nickname.c_str();
}

const char* Player::getPassword(void) const
{
    return this->password.c_str();
}

uint16_t Player::getPoints(void) const
{
    return this->points;
}

uint8_t Player::getLevel(void) const
{
    return this->passed_level;
}
