#ifndef MYSQLCPP_INCLUDE_PLAYER_HPP_
#define MYSQLCPP_INCLUDE_PLAYER_HPP_

#include <string>
#include <stdint.h>

class player
{
private:
    std::string nickname;
    std::string password;
    uint16_t points=0;
    uint8_t passed_level=0;
public:
    player();
    player(const std::string& nickname, const std::string& password);
    player(const char* nickname, const char* password);
    virtual ~player() = default;

    void setNickname(const std::string& nickname);
    void setNickname(const char* nickname);
    const char* getNickname(void) const;

    void setPassword(const std::string& password);
    void setPassword(const char* password);
    const char* getPassword(void) const;

    void setPoints(const uint16_t points);
    uint16_t getPoints(void) const;

    void setPassedLevel(const uint8_t passed_level);
    uint8_t getPassedLevel(void) const;
};

typedef player player_s;

#endif //MYSQLCPP_INCLUDE_PLAYER_HPP_