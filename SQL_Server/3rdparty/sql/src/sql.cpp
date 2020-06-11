#include "../include/sql.h"

#include <mysql/mysql.h>
#include <string.h>
#include <sstream>

namespace SQL_state_handler
{
    typedef enum {Error, Free, Reserved} checkRow_st;
};

class SQL::SQL_impl
{
private:
    MYSQL* m_connector;
    MYSQL* get_connection(const std::string& admin_username, const std::string& admin_password);
public:
    explicit SQL_impl() = default;
    virtual ~SQL_impl() = default;

    bool sync(const std::string& admin_username, const std::string& admin_password);

    SQL_state_handler::checkRow_st check_existance(const char* data, const char* typeOfData);
    SQL_state_handler::checkRow_st check_password(const char* password, const char* typeOfData, const char* data);
    bool insert_new_player(const std::string& player_mail, const std::string& player_username, const std::string& player_password);
    bool change_players_name(const std::string& player_mail, const std::string& new_username);
    bool change_players_password(const std::string& player_mail, const std::string& new_password);
    bool change_players_score(const std::string& player_mail, const uint16_t points, const uint8_t passed_level);
    std::vector<std::string> getLeaderboard(void) const;

};

//private SQL_impl
MYSQL* SQL::SQL_impl::get_connection(const std::string& admin_username, const std::string& admin_password)
{
    MYSQL* tmp;
    tmp = mysql_init(0);
    tmp = mysql_real_connect(tmp, "localhost", admin_username.c_str(), admin_password.c_str(), "game_database", 0, NULL, 0);
    return tmp;
}

//public SQL_impl
bool SQL::SQL_impl::sync(const std::string& admin_username, const std::string& admin_password)
{
    if(m_connector != nullptr) 
    {
        return true;
    }
    this->m_connector = SQL::SQL_impl::get_connection(admin_username, admin_password);
    return (m_connector != nullptr) ? true : false;
}

SQL_state_handler::checkRow_st SQL::SQL_impl::check_password(const char* password, const char* typeOfData, const char* data)
{
    MYSQL_RES* res;
    std::string message = "SELECT password FROM players WHERE " + std::string(typeOfData) + " = \"" + std::string(data) + '"';
    //std::cout << "message: " << message << '\n';
    int qstate = mysql_query(m_connector, message.c_str() );
    if(qstate)
    {
        //std::cout << "\nSelection request has been failed\n";
        return SQL_state_handler::Error;
    }
    res = mysql_store_result(m_connector);
    if(NULL == res)
    {
        //std::cout << "\nStoring request has been failed\n";
        return SQL_state_handler::Error;
    }
    MYSQL_ROW row = mysql_fetch_row(res);

    if(!strcmp(row[0], password) )
    {
        return SQL_state_handler::Reserved;
    }
    //std::cout << "Free\n";
    return SQL_state_handler::Free;
}

SQL_state_handler::checkRow_st SQL::SQL_impl::check_existance(const char* data, const char* typeOfData)
{
    if(strcmp(typeOfData,"nickname") && strcmp(typeOfData,"email") )
    {
        return SQL_state_handler::Error;
    }
    MYSQL_RES* res;
    std::string message = "SELECT " + std::string(typeOfData) + " FROM players";
    int qstate = mysql_query(m_connector, message.c_str() );
    if(qstate)
    {
        //std::cout << "\nSelection request has been failed\n";
        return SQL_state_handler::Error;
    }
    res = mysql_store_result(m_connector);
    if(NULL == res)
    {
        //std::cout << "\nStoring request has been failed\n";
        return SQL_state_handler::Error;
    }
    MYSQL_ROW row = mysql_fetch_row(res);
    while(row)
    {
        if(!strcmp(row[0], data) )
        {
            return SQL_state_handler::Reserved;
        }
        row = mysql_fetch_row(res);
    }
    return SQL_state_handler::Free;
}

bool SQL::SQL_impl::insert_new_player(const std::string& player_mail, const std::string& player_username, const std::string& player_password)
{
    int qstate;
    std::stringstream ss;
    ss << "INSERT INTO players(email, nickname, password, points, level) VALUES('" << \
        player_mail << "','" <<  player_username << "','" << player_password << "',0,0)";
    const auto query_st = ss.str(); //ss.str(""); ss.clear();
    auto* query = query_st.c_str();
    qstate = mysql_query(m_connector, query);
    //const std::string report = "\nRecording of " + player_username + " player has";
    if(qstate != 0)
    {
        //std::cout << report << " been failed...\n";
        return false;
    }
    //std::cout << report << " succeed...\n";
    return true;
}

bool SQL::SQL_impl::change_players_name(const std::string& player_mail, const std::string& new_username)
{
    std::stringstream ss;
    ss << "UPDATE players set nickname = '" << new_username << "' where email = '" << player_mail << "'";
    const auto query_st = ss.str(); //ss.str(""); ss.clear();
    auto* query = query_st.c_str();
    int qstate = mysql_query(m_connector, query);
    if(qstate)
    {
        //std::cout << "\nRename request has been failed\n";
        return false;
    }
    //std::cout << "\nPlayer succesfully renamed from: " << oldName << " to: " << player->getNickname() << '\n';
    return true;
}

bool SQL::SQL_impl::change_players_password(const std::string& player_mail, const std::string& new_password)
{
    std::stringstream ss;
    ss << "UPDATE players set password = '" << new_password << "' where email = '" << player_mail << "'";
    const auto query_st = ss.str(); //ss.str(""); ss.clear();
    auto* query = query_st.c_str();
    int qstate = mysql_query(m_connector, query);
    if(qstate)
    {
        //std::cout << "\nChange password request has been failed\n";
        return false;
    }
    //std::cout << "\nPlayer succesfully changed password\n";
    return true;
}

bool SQL::SQL_impl::change_players_score(const std::string& player_mail, const uint16_t points, const uint8_t passed_level)
{
    std::stringstream ss;
    ss << "UPDATE players SET points = " << (int)points << ", level = " << (int)passed_level << " where email = '" << player_mail << "'";
    const auto query_st = ss.str(); //ss.str(""); ss.clear();
    auto* query = query_st.c_str();
    int qstate = mysql_query(m_connector, query);
    if(qstate)
    {
        //std::cout << "\nChange score request has been failed\n";
        return false;
    }
    //std::cout << "\nPlayer succesfully updated score\n";
    return true;
}

std::vector<std::string> SQL::SQL_impl::getLeaderboard(void) const
{
    std::vector<std::string>tmp;
    MYSQL_RES* res;
    int qstate = mysql_query(m_connector, "SELECT nickname, points, level FROM players");
    if(qstate)
    {
        //std::cout << "\nSelection request has been failed\n";
        return tmp;
    }
    res = mysql_store_result(m_connector);
    if(NULL == res)
    {
        //std::cout << "\nStoring request has been failed\n";
        return tmp;
    }
    MYSQL_ROW row = mysql_fetch_row(res);
    while(row && tmp.size() < 9)
    {
        tmp.push_back(std::string(row[0]) + ' ' + std::string(row[1]) + ' ' + std::string(row[2]) );
        row = mysql_fetch_row(res);
    }
    return tmp;
}

//public SQL
SQL::SQL() :
    SQL_pimpl{std::make_unique<SQL_impl>() }
{

}

SQL::~SQL() = default;


bool SQL::sync(const std::string& admin_username, const std::string& admin_password)
{
    static bool synced = false;
    if(synced)
        return true;

    synced = SQL_pimpl->sync(admin_username, admin_password);
    return synced;
}

bool SQL::sign_in(const std::string& player_username_mail, const std::string& player_password)
{
    bool already_failed = false;

    auto check = SQL_pimpl->check_existance(player_username_mail.c_str(), "nickname" );
    if(check == SQL_state_handler::Error || check == SQL_state_handler::Free)
    {
        //std::cout << "\nPlayer with nickname: " << player->getNickname() << " does not exist!\n";
        //return false;
        already_failed = true;
    }

    if(already_failed)
    {
      check = SQL_pimpl->check_existance(player_username_mail.c_str(), "email" );
      if(check == SQL_state_handler::Error || check == SQL_state_handler::Free)
      {
          //std::cout << "\nPlayer with email: " << player->getEmail() << " does not exist!\n";
          return false;
      }
    }
    const char* type_of_data = (!already_failed) ? "nickname" : "email";
    check = SQL_pimpl->check_password(player_password.c_str(), type_of_data,  player_username_mail.c_str());
    if(check == SQL_state_handler::Error || check == SQL_state_handler::Free)
    {
        //std::cout << "\nIncorrect password\n";
        return false;
    }
    return true;
}

bool SQL::insert_new_player(const std::string& player_mail, const std::string& player_username, const std::string& player_password)
{
    //check does it exist allready
    auto check = SQL_pimpl->check_existance(player_username.c_str(), "nickname" );
    if(check == SQL_state_handler::Error || check == SQL_state_handler::Reserved)
    {
        //std::cout << "\nPlayer with nickname: " << player->getNickname() << " already exist!\n";
        return false;
    }
    check = SQL_pimpl->check_existance(player_mail.c_str(), "email" );
    if(check == SQL_state_handler::Error || check == SQL_state_handler::Reserved)
    {
        //std::cout << "\nPlayer with email: " << player->getEmail() << " already exist!\n";
        return false;
    }
    return SQL_pimpl->insert_new_player(player_mail, player_username, player_password);
}

bool SQL::change_players_name(const std::string& player_mail, const std::string& player_password, const std::string& new_username)
{
    //check does it exist allready
    auto check = SQL_pimpl->check_existance(player_mail.c_str(), "email" );
    if(SQL_state_handler::Error == check || SQL_state_handler::Free == check)
    {
        //std::cout << "\nPlayer with this email don't exists!\n";
        return false;
    }
    check = SQL_pimpl->check_password(player_password.c_str(), "email", player_mail.c_str() );
    if(SQL_state_handler::Error == check || SQL_state_handler::Free == check)
    {
        //std::cout << "\nPlayer with this email don't exists!\n";
        return false;
    }
    check = SQL_pimpl->check_existance(new_username.c_str(), "nickname");
    if(SQL_state_handler::Error == check || SQL_state_handler::Reserved == check)
    {
        //std::cout << "\nSuggested new nickname has already exists!\n";
        return false;
    }
    return SQL_pimpl->change_players_name(player_mail, new_username);
}

bool SQL::change_players_password(const std::string& player_mail, const std::string& player_password, const std::string& new_password)
{
    //check does it exist already
    auto check = SQL_pimpl->check_existance(player_mail.c_str(), "email" );
    if(SQL_state_handler::Error == check || SQL_state_handler::Free == check)
    {
        //std::cout << "\nPlayer with this nickname don't exists!\n";
        return false;
    }
    check = SQL_pimpl->check_password(player_password.c_str(), "email", player_mail.c_str() );
    if(SQL_state_handler::Error == check || SQL_state_handler::Free == check)
    {
        //std::cout << "\nPlayer with this email don't exists!\n";
        return false;
    }
    return SQL_pimpl->change_players_password(player_mail, new_password);
}

bool SQL::change_players_score(const std::string& player_mail, const uint16_t points, const uint8_t passed_level)
{
    if( !(points <= (uint16_t)500) || !(passed_level <= (uint8_t)5) )
    {
        //std::cout << "\nBad level or points!\n";
        return false;
    }
    //check does it exist already
    const auto check = SQL_pimpl->check_existance(player_mail.c_str(), "email" );
    if(SQL_state_handler::Error == check || SQL_state_handler::Free == check)
    {
        //std::cout << "\nPlayer with this nickname don't exists!\n";
        return false;
    }

    return SQL_pimpl->change_players_score(player_mail, points, passed_level);
}

std::vector<std::string> SQL::getLeaderboard(void) const
{
    return SQL_pimpl->getLeaderboard();
}
