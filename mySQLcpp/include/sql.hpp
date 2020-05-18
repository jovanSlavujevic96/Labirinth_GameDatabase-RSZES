#ifndef MYSQLCPP_INCLUDE_SQL_HPP_
#define MYSQLCPP_INCLUDE_SQL_HPP_

#include <memory>
#include "player.hpp"

class SQL
{
private:
    class SQL_impl;
    std::unique_ptr<SQL_impl> SQL_pimpl;
public:
    explicit SQL(const char* username, const char* password);
    virtual ~SQL();

    bool insert_new_player(const player_s* player);
    bool change_players_name(player_s* player, const char* newName);
    bool change_players_password(player_s* player, const char* newPassword);
    bool change_players_score(player_s* player, const uint16_t points, const uint8_t passed_level);
    void print_leaderboard(void);
};

#endif //MYSQLCPP_INCLUDE_SQL_HPP_
