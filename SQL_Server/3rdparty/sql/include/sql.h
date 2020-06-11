#ifndef SQL_H
#define SQL_H

#include <memory>
#include <vector>
#include <string>
#include <stdint.h>

class SQL
{
private:
    class SQL_impl;
    std::unique_ptr<SQL_impl> SQL_pimpl;
public:
    explicit SQL();
    virtual ~SQL();

    bool sync(const std::string& admin_username, const std::string& admin_password);
    bool sign_in(const std::string& player_username_mail, const std::string& player_password);
    bool insert_new_player(const std::string& player_mail, const std::string& player_username, const std::string& player_password);
    bool change_players_name(const std::string& player_mail, const std::string& player_password, const std::string& new_username);
    bool change_players_password(const std::string& player_mail, const std::string& player_password, const std::string& new_password);
    bool change_players_score(const std::string& player_mail, const uint16_t points, const uint8_t passed_level);
    std::vector<std::string> getLeaderboard(void) const;   
};

#endif // SQL_H
