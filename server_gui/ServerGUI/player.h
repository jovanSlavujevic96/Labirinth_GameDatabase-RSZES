#ifndef PLAYER_H
#define PLAYER_H

#include <stdint.h>
#include <string>

class Player
{
private:
    std::string email, nickname, password;
    uint16_t points=0;
    uint8_t passed_level=0;

public:
    explicit Player() = default;
    explicit Player(const std::string& email, const std::string& nickname, const std::string& password);
    explicit Player(const char* email, const char* nickname, const char* password);
    virtual ~Player() = default;

    //setters
    void setNickname(const std::string& nickname);
    void setNickname(const char* nickname);
    void setPassword(const std::string& password);
    void setPassword(const char* password);
    void setPoints(const uint16_t points);
    void setLevel(const uint8_t passed_level);

    //getters
    const char* getNickname(void) const;
    const char* getPassword(void) const;
    const char* getEmail(void) const;
    uint16_t getPoints(void) const;
    uint8_t getLevel(void) const;
};

typedef Player player_s;

#endif // PLAYER_H
