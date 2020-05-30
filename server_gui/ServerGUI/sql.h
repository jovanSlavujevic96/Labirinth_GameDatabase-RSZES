#ifndef SQL_H
#define SQL_H

#include "player.h"
#include <memory>
#include <vector>

class SQL
{
    class SQL_impl;
    std::unique_ptr<SQL_impl> SQL_pimpl;
    bool synced = false;

public:
    explicit SQL();
    virtual ~SQL();

    bool sync(const char* username, const char* password);
    bool sign_in(player_s* player);
    bool insert_new_player(const player_s* player);
    bool change_players_name(player_s* player, const char* newName);
    bool change_players_password(player_s* player, const char* newPassword);
    bool change_players_score(player_s* player, const uint16_t points, const uint8_t passed_level);
    std::vector<std::string> getLeaderboard(void) const;
};

#endif // SQL_H
