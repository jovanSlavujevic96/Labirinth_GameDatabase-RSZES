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
    std::string sign_in(const std::string& player_username_mail, const std::string& player_password);
    std::string insert_new_player(const std::string& player_mail, const std::string& player_username, const std::string& player_password);
    std::string change_players_name(const std::string& player_mail, const std::string& player_password, const std::string& new_username);
    std::string change_players_password(const std::string& player_mail, const std::string& player_password, const std::string& new_password);
    std::string change_players_score(const std::string& player_mail, const uint16_t points, const uint8_t passed_level);
    bool generateXMLfile(const char* filename);
    std::vector<std::string> getLeaderboard(void) const;   
};

#endif // SQL_H
