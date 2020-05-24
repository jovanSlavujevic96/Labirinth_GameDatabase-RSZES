#include <sql.hpp>

#include <mysql/mysql.h>
#include <iostream>
#include <sstream>
#include <string.h>

typedef enum {Error, Free, Reserved} checkRow_st;

class SQL::SQL_impl
{
private:
    MYSQL* m_connector;

    MYSQL* get_connection(const char* username, const char* password);
public:
    SQL_impl(const char* username, const char* password);
    virtual ~SQL_impl() = default;

    checkRow_st check_existance(const char* data, const char* typeOfData);
    bool insert_new_player(const player_s* player);
    bool change_players_name(player_s* player, const char* newName);
    bool change_players_password(player_s* player, const char* newPassword);
    bool change_players_score(player_s* player, const uint16_t points, const uint8_t passed_level);
    void print_leaderboard(void);
};

MYSQL* SQL::SQL_impl::get_connection(const char* username, const char* password)
{
    MYSQL* tmp;
    tmp = mysql_init(0);
    tmp = mysql_real_connect(tmp, "localhost", username, password, "game_database", 0, NULL, 0);
    return tmp;
}

SQL::SQL_impl::SQL_impl(const char* username, const char* password)
{
    m_connector = get_connection(username, password);
    if(!m_connector)
    {
        std::cout << "\nConnection has not succeed\n";
        std::exit(-1);
    }
    std::cout << "\nConnection has succeed\n";
}

checkRow_st SQL::SQL_impl::check_existance(const char* data, const char* typeOfData)
{
    if(strcmp(typeOfData,"nickname") && strcmp(typeOfData,"email") )
    {
        return Error;
    }
    MYSQL_RES* res;
    std::string message = "SELECT " + std::string(typeOfData) + " FROM players";
    int qstate = mysql_query(m_connector, message.c_str() );
    if(qstate)
    {
        std::cout << "\nSelection request has been failed\n";
        return Error;
    }
    res = mysql_store_result(m_connector);
    if(NULL == res) 
    {
        std::cout << "\nStoring request has been failed\n";
        return Error;
    }
    MYSQL_ROW row = mysql_fetch_row(res);
    while(row)
    {
        if(!strcmp(row[0], data) )
        {
            return Reserved;    
        }
        row = mysql_fetch_row(res);
    }
    return Free;    
}

bool SQL::SQL_impl::insert_new_player(const player_s* player)
{
    int qstate;
    std::stringstream ss;
    ss << "INSERT INTO players(email, nickname, password, points, level) VALUES('" << \
        player->getEmail() << "','" <<  player->getNickname() << "','" << player->getPassword() << "',0,0)";
    const auto query_st = ss.str(); //ss.str(""); ss.clear();
    auto* query = query_st.c_str();
    qstate = mysql_query(m_connector, query);
    const std::string report = "\nRecording of " + std::string(player->getNickname()) + " player has";
    if(qstate != 0)
    {
        std::cout << report << " been failed...\n";
        return false;
    }
    std::cout << report << " succeed...\n";
    return true;
}

bool SQL::SQL_impl::change_players_name(player_s* player, const char* newName)
{
    const std::string oldName = player->getNickname();
    std::stringstream ss;
    ss << "UPDATE players set nickname = '" << newName << "' where email = '" << player->getEmail() << "'";
    const auto query_st = ss.str(); //ss.str(""); ss.clear();
    auto* query = query_st.c_str();
    int qstate = mysql_query(m_connector, query);
    if(qstate)
    {
        std::cout << "\nRename request has been failed\n";
        return false;
    }
    player->setNickname(newName);
    std::cout << "\nPlayer succesfully renamed from: " << oldName << " to: " << player->getNickname() << '\n';
    return true;
}

bool SQL::SQL_impl::change_players_password(player_s* player, const char* newPassword)
{
    std::stringstream ss;
    ss << "UPDATE players set password = '" << newPassword << "' where email = '" << player->getEmail() << "'";
    const auto query_st = ss.str(); //ss.str(""); ss.clear();
    auto* query = query_st.c_str();
    int qstate = mysql_query(m_connector, query);
    if(qstate)
    {
        std::cout << "\nChange password request has been failed\n";
        return false;
    }
    player->setPassword(newPassword);
    std::cout << "\nPlayer succesfully changed password\n";
    return true;
}

bool SQL::SQL_impl::change_players_score(player_s* player, const uint16_t points, const uint8_t passed_level)
{
    std::stringstream ss;
    ss << "UPDATE players SET points = " << (int)points << ", level = " << (int)passed_level << " where nickname = '" << player->getNickname() << "'";
    const auto query_st = ss.str(); //ss.str(""); ss.clear();
    auto* query = query_st.c_str();
    int qstate = mysql_query(m_connector, query);
    if(qstate)
    {
        std::cout << "\nChange score request has been failed\n";
        return false;
    }
    player->setPoints(points);
    player->setPassedLevel(passed_level);
    std::cout << "\nPlayer succesfully updated score\n";
    return true;
}

void SQL::SQL_impl::print_leaderboard(void)
{
    MYSQL_RES* res;
    int qstate = mysql_query(m_connector, "SELECT nickname, points, level FROM players");
    if(qstate)
    {
        std::cout << "\nSelection request has been failed\n";
        return;
    }
    res = mysql_store_result(m_connector);
    if(NULL == res) 
    {
        std::cout << "\nStoring request has been failed\n";
        return;
    }
    MYSQL_ROW row = mysql_fetch_row(res);
    while(row)
    {
        std::cout << row[0] << ' ' << row[1] << ' ' << row[2] << '\n';  
        row = mysql_fetch_row(res);
    }
}

SQL::SQL(const char* username, const char* password) : 
    SQL_pimpl{std::make_unique<SQL::SQL_impl>(username, password)}
{

}

SQL::~SQL() = default;

bool SQL::sign_in(player_s* player)
{
    if(!player)
    {
        return false;
    }

    auto check = SQL_pimpl->check_existance(player->getNickname(), "nickname" );
    if(check == Error || check == Free)
    {
        std::cout << "\nPlayer with nickname: " << player->getNickname() << " does not exist!\n";
        return false;
    }
    check = SQL_pimpl->check_existance(player->getEmail(), "email" );
    if(check == Error || check == Free)
    {
        std::cout << "\nPlayer with email: " << player->getEmail() << " does not exist!\n";
        return false;
    }
    return true;
}

bool SQL::insert_new_player(const player_s* player)
{
    if(!player)
        return false;

    //check does it exist allready
    auto check = SQL_pimpl->check_existance(player->getNickname(), "nickname" );
    if(check == Error || check == Reserved)
    {
        std::cout << "\nPlayer with nickname: " << player->getNickname() << " already exist!\n";
        return false;
    }
    check = SQL_pimpl->check_existance(player->getEmail(), "email" );
    if(check == Error || check == Reserved)
    {
        std::cout << "\nPlayer with email: " << player->getEmail() << " already exist!\n";
        return false;
    }
    return SQL_pimpl->insert_new_player(player);
}

bool SQL::change_players_name(player_s* player, const char* newName)
{
    if(!player)
        return false;

    //check does it exist allready
    auto check = SQL_pimpl->check_existance(player->getNickname(), "nickname" );
    if(Error == check || Free == check)
    {
        std::cout << "\nPlayer with this nickname don't exists!\n";
        return false;
    }
    check = SQL_pimpl->check_existance(player->getEmail(), "email" );
    if(Error == check || Free == check)
    {
        std::cout << "\nPlayer with this email don't exists!\n";
        return false;
    }
    check = SQL_pimpl->check_existance(newName, "nickname");
    if(Error == check || Reserved == check)
    {
        std::cout << "\nSuggested new nickname has already exists!\n";
        return false;
    }
    return SQL_pimpl->change_players_name(player, newName);
}

bool SQL::change_players_password(player_s* player, const char* newPassword)
{
    if(!player)
        return false;

    //check does it exist already
    const auto check = SQL_pimpl->check_existance(player->getNickname(), "nickname" );
    if(Error == check || Free == check)
    {
        std::cout << "\nPlayer with this nickname don't exists!\n";
        return false;
    }

    return SQL_pimpl->change_players_password(player, newPassword);
}

bool SQL::change_players_score(player_s* player, const uint16_t points, const uint8_t passed_level)
{
    if( !(points >= 0 && points <= 500) || !(passed_level >= 0 && passed_level <= 5) )
    {
        std::cout << "\nBad level or points!\n";
        return false;
    }

    if(!player)
        return false;

    //check does it exist already
    const auto check = SQL_pimpl->check_existance(player->getNickname(), "nickname" );
    if(Error == check || Free == check)
    {
        std::cout << "\nPlayer with this nickname don't exists!\n";
        return false;
    }

    return SQL_pimpl->change_players_score(player, points, passed_level);
}

void SQL::print_leaderboard(void)
{
    SQL_pimpl->print_leaderboard();
}