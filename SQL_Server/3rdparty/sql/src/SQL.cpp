#include "../include/SQL.h"

//3rd party libs
#include <mysql/mysql.h>
#include <tinyxml.h>

//C libs
#include <unistd.h>
#include <string.h>
#include <stdint.h>

//STL CPP libs
#include <thread>
#include <mutex>
#include <condition_variable>
#include <atomic>
#include <chrono>
#include <ctime>

namespace SQL_enum_handler
{
    enum class checkRow_st{Error, Free, Reserved};
};

namespace SQL_MThandler
{
    static std::mutex mutex;
    static std::condition_variable condition_var;
    static volatile std::atomic<bool> timerOn {true}, ping {false}, counting {false};
};

#define err "ERR_"
#define errSrv err "SRV"
#define errBName err "BAD_NAME"
#define errBMail err "BAD_MAIL"
#define errBPass err "BAD_PASS"
#define errUName err "USED_NAME"
#define errUMail err "USED_MAIL"
#define errUPass err "USED_PASS"
#define errBPts err "BAD_PTS"
#define ok "OK"

#define BUFFER_SIZE 1024
#define LDB_FILENAME "leaderboard.xml"
#define TimerCounterLimit (0xf4240/100) //0xf4240 -> million

class SQL::SQL_impl
{
protected:
    std::string m_SQLuser, m_SQLpass;

private:
    MYSQL* m_connector;
    std::vector<IObserver*> m_observers;
    std::vector<std::string> m_ldbName, m_ldbLevel, m_ldbPoints;
    std::unique_ptr<Data> m_data;
    std::thread m_timerTH, m_pingTH, m_updateTH;
    std::atomic<bool> m_SignedIn;

    MYSQL* get_connection(void);
    void interruptTimer(void);
    void pingSQL(void);
    void updateLeaderboard(bool& notify);
    bool generateXML(void);
    void notify(void);

public:
    explicit SQL_impl();
    virtual ~SQL_impl();

    bool sync(const std::string& admin_username, const std::string& admin_password);
    void attach(IObserver *observer);

    SQL_enum_handler::checkRow_st check_existance(const char* data, const char* typeOfData);
    SQL_enum_handler::checkRow_st check_password(const char* password, const char* typeOfData, const char* data);
    bool insert_new_player(const std::string& player_mail, const std::string& player_username, const std::string& player_password);
    bool change_players_name(const std::string& player_mail, const std::string& new_username);
    bool change_players_email(const std::string& player_mail, const std::string& new_mail);
    bool change_players_password(const std::string& player_mail, const std::string& new_password);
    bool change_players_score(const std::string& player_mail, const uint16_t points, const uint8_t passed_level);
    std::vector<std::string> getPlayerData(const char* data, const char* typeOfData);
};

//private SQL_impl
MYSQL* SQL::SQL_impl::get_connection(void)
{
    MYSQL* tmp;
    tmp = mysql_init(0);
    tmp = mysql_real_connect(tmp, "localhost", m_SQLuser.c_str(), m_SQLpass.c_str(), "game_database", 0, NULL, 0);
    return tmp;
}

void SQL::SQL_impl::interruptTimer(void)
{
    uint32_t timerIncr = 0;
    uint8_t secondIncr = 0;
    uint8_t minuteIncr = 0;
    while(true == static_cast<bool>(SQL_MThandler::timerOn) )
    {
        (void)usleep(50);
        if( (++timerIncr) == TimerCounterLimit )
        {
            timerIncr = 0;
            if((++secondIncr) == 60)
            {
                secondIncr = 0;
                if((++minuteIncr) == 30)
                {
                    minuteIncr = 0;
                    SQL_MThandler::ping = true;
                }
            }
            SQL_MThandler::counting = false;
            SQL_MThandler::condition_var.notify_all();
        }
    }
    SQL_MThandler::counting = false;
    SQL_MThandler::condition_var.notify_one();
}

void SQL::SQL_impl::notify(void)
{    
    bool notify;
    while(true == static_cast<bool>(SQL_MThandler::timerOn) )
    {
        std::unique_lock<std::mutex> lk(SQL_MThandler::mutex);
        SQL_MThandler::condition_var.wait(lk, []{
                return(false == static_cast<bool>(SQL_MThandler::counting) ||
                false == static_cast<bool>(SQL_MThandler::timerOn) );
            } );
            
        if(false == static_cast<bool>(SQL_MThandler::timerOn) )
        {
            break;
        }
        SQL::SQL_impl::updateLeaderboard(notify);
        if(notify)
        {
            SQL_impl::generateXML();
            for(auto& observer : m_observers)
            {
                observer->Update(*m_data.get() );
            }
        }
        SQL_MThandler::counting = true;
        SQL_MThandler::condition_var.notify_one();
    }
    SQL_MThandler::counting = true;
    SQL_MThandler::condition_var.notify_one();
}

void SQL::SQL_impl::pingSQL(void)
{       
    while(true == static_cast<bool>(SQL_MThandler::timerOn) )
    {
        std::unique_lock<std::mutex> lk(SQL_MThandler::mutex);
        SQL_MThandler::condition_var.wait(lk, []{ return(true == static_cast<bool>(SQL_MThandler::ping) ); } );
    
        if(true == static_cast<bool>(SQL_MThandler::timerOn))
        {
            std::time_t timestamp = std::chrono::system_clock::to_time_t(std::chrono::system_clock::now() );
            std::cout << "pingSQL :: " << std::ctime(&timestamp) << '\n';
        
            this->m_connector = SQL_impl::get_connection();
        }    
        SQL_MThandler::ping = false;
        SQL_MThandler::condition_var.notify_one();
    }
    SQL_MThandler::condition_var.notify_one();
}

void SQL::SQL_impl::updateLeaderboard(bool& notify)
{
    MYSQL_RES* res;
    const char* command = "SELECT nickname, level, points FROM players ORDER BY points DESC LIMIT 10";
    int qstate = mysql_query(m_connector, command);
    if(qstate)
    {
        //std::cout << "\nSelection request has been failed\n";
        return;
    }
    res = mysql_store_result(m_connector);
    if(NULL == res)
    {
        //std::cout << "\nStoring request has been failed\n";
        return;
    }
    MYSQL_ROW row = mysql_fetch_row(res);
    uint8_t incr = 0;
    notify = false;
    while( row && incr < 10)
    {
        if(incr == m_ldbName.size() )
        {
            m_ldbName.push_back(row[0] );
            m_ldbPoints.push_back(row[2] );
            m_ldbLevel.push_back(row[1] );
            notify = true;
        }
        else //incr < m_ldbName.size()
        {
            if( strcmp(m_ldbName[incr].c_str(), row[0]) ||
                strcmp(m_ldbPoints[incr].c_str(), row[2]) ||
                strcmp(m_ldbLevel[incr].c_str(), row[1]) ) 
            {
                m_ldbName[incr] = row[0];
                m_ldbPoints[incr] = row[2];
                m_ldbLevel[incr] = row[1];
                notify = true;
            }
        }
        row = mysql_fetch_row(res);
        incr++;
    }
    if(static_cast<int64_t>(m_ldbName.size() ) > static_cast<int64_t>(incr) )
    {
        m_ldbName.erase(m_ldbName.begin()+incr, m_ldbName.end() );
        m_ldbPoints.erase(m_ldbPoints.begin()+incr, m_ldbPoints.end() );
        m_ldbLevel.erase(m_ldbLevel.begin()+incr, m_ldbLevel.end() );
        notify = true;
    }
}

bool SQL::SQL_impl::generateXML(void)
{
    TiXmlDocument doc; 
    doc.LinkEndChild( new TiXmlDeclaration("1.0", "", "") );
        
    TiXmlElement * root = new TiXmlElement( "leaderboard" );
    for(uint8_t i=0; i<m_ldbName.size(); ++i)
    {
        TiXmlElement * child = new TiXmlElement( "player" );  
        child->SetAttribute("points", m_ldbPoints[i] );
        child->SetAttribute("name", m_ldbName[i] );
        child->SetAttribute("level", m_ldbLevel[i] );
        
        /* //element instead of attribute
        {   
            TiXmlElement * element = new TiXmlElement( "name" );
            TiXmlText * text = new TiXmlText( m_ldbName[i] );
	        element->LinkEndChild( text );
            child->LinkEndChild(std::move(element) );
        }
        */
        root->LinkEndChild(std::move(child) );
    }
    doc.LinkEndChild(std::move(root) );
    return doc.SaveFile(m_data->FILENAME); 
}

SQL::SQL_impl::SQL_impl() : 
    m_data{std::make_unique<Data>() },
    m_SignedIn{false}
{
    m_data->name = &m_ldbName;
    m_data->level = &m_ldbLevel;
    m_data->points = &m_ldbPoints;
    m_data->FILENAME = LDB_FILENAME;
}

SQL::SQL_impl::~SQL_impl()
{
    m_timerTH.detach();
    m_pingTH.detach();
    m_updateTH.detach();
}

//public SQL_impl
bool SQL::SQL_impl::sync(const std::string& admin_username, const std::string& admin_password)
{
    this->m_SQLuser = admin_username;
    this->m_SQLpass = admin_password;
    this->m_connector = SQL::SQL_impl::get_connection();
    
    if(m_connector != nullptr)
    {
        m_timerTH = std::thread(&SQL::SQL_impl::interruptTimer, this);
        m_pingTH = std::thread(&SQL::SQL_impl::pingSQL, this);
        m_updateTH = std::thread(&SQL::SQL_impl::notify, this);
        return true;
    }
    return false;
}

SQL_enum_handler::checkRow_st SQL::SQL_impl::check_password(const char* password, const char* typeOfData, const char* data)
{
    MYSQL_RES* res;
    std::string message = "SELECT password FROM players WHERE " + std::string(typeOfData) + " = \"" + std::string(data) + '"';
    //std::cout << "message: " << message << '\n';
    int qstate = mysql_query(m_connector, message.c_str() );
    if(qstate)
    {
        //std::cout << "\nSelection request has been failed\n";
        return SQL_enum_handler::checkRow_st::Error;
    }
    res = mysql_store_result(m_connector);
    if(NULL == res)
    {
        //std::cout << "\nStoring request has been failed\n";
        return SQL_enum_handler::checkRow_st::Error;
    }
    MYSQL_ROW row = mysql_fetch_row(res);

    if(!strcmp(row[0], password) )
    {
        return SQL_enum_handler::checkRow_st::Reserved;
    }
    //std::cout << "Free\n";
    return SQL_enum_handler::checkRow_st::Free;
}

SQL_enum_handler::checkRow_st SQL::SQL_impl::check_existance(const char* data, const char* typeOfData)
{
    if(strcmp(typeOfData,"nickname") && strcmp(typeOfData,"email") )
    {
        return SQL_enum_handler::checkRow_st::Error;
    }
    MYSQL_RES* res;
    std::string message = "SELECT " + std::string(typeOfData) + " FROM players";
    int qstate = mysql_query(m_connector, message.c_str() );
    if(qstate)
    {
        //std::cout << "\nSelection request has been failed\n";
        return SQL_enum_handler::checkRow_st::Error;
    }
    res = mysql_store_result(m_connector);
    if(NULL == res)
    {
        //std::cout << "\nStoring request has been failed\n";
        return SQL_enum_handler::checkRow_st::Error;
    }
    MYSQL_ROW row = mysql_fetch_row(res);
    while(row)
    {
        if(!strcmp(row[0], data) )
        {
            return SQL_enum_handler::checkRow_st::Reserved;
        }
        row = mysql_fetch_row(res);
    }
    return SQL_enum_handler::checkRow_st::Free;
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

bool SQL::SQL_impl::change_players_email(const std::string& player_mail, const std::string& new_mail)
{
    std::stringstream ss;
    ss << "UPDATE players set email = '" << new_mail << "' where email = '" << player_mail << "'";
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

std::vector<std::string> SQL::SQL_impl::getPlayerData(const char* data, const char* typeOfData)
{
    std::vector<std::string> dataVec;
    std::string email, nickname;
    std::string select = "email";
    if (!strcmp("email", typeOfData) )
    {
        email = data;
        select = "nickname";
    }
    else if(!strcmp("nickname", typeOfData))
    {
        nickname = data;
    }

    MYSQL_RES* res;
    std::string message = "SELECT " + select + ",points,level FROM players WHERE " + std::string(typeOfData) + " = \"" + std::string(data) + '"';
    int qstate = mysql_query(m_connector, message.c_str() );
    if(qstate)
    {
        //std::cout << "\nSelection request has been failed\n";
        dataVec.push_back(err);
        return dataVec;
    }
    res = mysql_store_result(m_connector);
    if(NULL == res)
    {
        //std::cout << "\nStoring request has been failed\n";
        dataVec.push_back(err);
        return dataVec;
    }
    MYSQL_ROW row = mysql_fetch_row(res);
    if (!strcmp("email", typeOfData) )
    {
        nickname = row[0];
    }
    else if(!strcmp("nickname", typeOfData))
    {
        email = row[0];
    }
    dataVec.push_back(email);
    dataVec.push_back(nickname);
    dataVec.push_back(row[1]);
    dataVec.push_back(row[2]);
    return dataVec;
}

void SQL::SQL_impl::attach(IObserver *observer)
{
    m_observers.push_back(observer);
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

    return (synced = SQL_pimpl->sync(admin_username, admin_password) );
}

std::string SQL::sign_in(const std::string& player_username_mail, const std::string& player_password)
{
    std::string data_type = "nickname";
    for(const auto& letter: player_username_mail)
    {
        if('@' == letter)
        {
            data_type = "email";
            break;
        }
    }

    auto check = SQL_pimpl->check_existance(player_username_mail.c_str(), data_type.c_str() );
    if(SQL_enum_handler::checkRow_st::Error == check) 
    {
        return errSrv;
    }
    else if(SQL_enum_handler::checkRow_st::Free == check)
    {
        return data_type == "nickname" ? errBName : errBMail;
    }
    
    check = SQL_pimpl->check_password(player_password.c_str(), data_type.c_str(),  player_username_mail.c_str());
    if(SQL_enum_handler::checkRow_st::Error == check) 
    {
        return errSrv;
    }
    else if(SQL_enum_handler::checkRow_st::Free == check)
    {
        return errBPass;
    }
    return ok;
}

std::string SQL::insert_new_player(const std::string& player_mail, const std::string& player_username, const std::string& player_password)
{
    //check does it exist allready
    auto check = SQL_pimpl->check_existance(player_username.c_str(), "nickname" );
    if(SQL_enum_handler::checkRow_st::Error == check)
    {
        return errSrv;
    }
    else if (check == SQL_enum_handler::checkRow_st::Reserved)
    {
        return errUName;
    }

    check = SQL_pimpl->check_existance(player_mail.c_str(), "email" );
    if(SQL_enum_handler::checkRow_st::Error == check) 
    {
        return errSrv;
    }
    else if(SQL_enum_handler::checkRow_st::Reserved == check)
    {
        return errUMail;
    }

    return (true == SQL_pimpl->insert_new_player(player_mail, player_username, player_password) ) ? ok : errSrv;
}

std::string SQL::change_players_name(const std::string& player_mail, const std::string& player_password, const std::string& new_username)
{
    //check does it exist allready
    auto check = SQL_pimpl->check_existance(player_mail.c_str(), "email" );
    if(SQL_enum_handler::checkRow_st::Error == check) 
    {
        return errSrv;
    }
    else if(SQL_enum_handler::checkRow_st::Free == check)
    {
        return errBMail;
    }

    check = SQL_pimpl->check_password(player_password.c_str(), "email", player_mail.c_str() );
    if(SQL_enum_handler::checkRow_st::Error == check) 
    {
        return errSrv;
    }
    else if(SQL_enum_handler::checkRow_st::Free == check)
    {
        return errBPass;
    }

    check = SQL_pimpl->check_existance(new_username.c_str(), "nickname");
    if(SQL_enum_handler::checkRow_st::Error == check)
    {
        return errSrv;
    }
    else if(SQL_enum_handler::checkRow_st::Reserved == check)
    {
        return errUName;
    }

    return (true == SQL_pimpl->change_players_name(player_mail, new_username)) ? ok : errSrv;
}

std::string SQL::change_players_email(const std::string& player_mail, const std::string& player_password, const std::string& new_email)
{
    //check does it exist already
    auto check = SQL_pimpl->check_existance(player_mail.c_str(), "email" );
    if(SQL_enum_handler::checkRow_st::Error == check)
    {
        return errSrv;
    }
    else if (SQL_enum_handler::checkRow_st::Free == check)
    {
        return errBMail;
    }

    check = SQL_pimpl->check_password(player_password.c_str(), "email", player_mail.c_str() );
    if(SQL_enum_handler::checkRow_st::Error == check)
    {
        return errSrv;
    }
    else if(SQL_enum_handler::checkRow_st::Free == check)
    {
        return errBPass;
    }

    check = SQL_pimpl->check_existance(new_email.c_str(), "email" );
    if(SQL_enum_handler::checkRow_st::Error == check)
    {
        return errSrv;
    }
    else if (SQL_enum_handler::checkRow_st::Free == check)
    {
        return errUMail;
    }
    return (true == SQL_pimpl->change_players_email(player_mail, new_email)) ? ok : errSrv;
}

std::string SQL::change_players_password(const std::string& player_mail, const std::string& player_password, const std::string& new_password)
{
    //check does it exist already
    auto check = SQL_pimpl->check_existance(player_mail.c_str(), "email" );
    if(SQL_enum_handler::checkRow_st::Error == check)
    {
        return errSrv;
    }
    else if (SQL_enum_handler::checkRow_st::Free == check)
    {
        return errBMail;
    }

    check = SQL_pimpl->check_password(player_password.c_str(), "email", player_mail.c_str() );
    if(SQL_enum_handler::checkRow_st::Error == check)
    {
        return errSrv;
    }
    else if(SQL_enum_handler::checkRow_st::Free == check)
    {
        return errBPass;
    }
    return (true == SQL_pimpl->change_players_password(player_mail, new_password)) ? ok : errSrv;
}

std::string SQL::change_players_score(const std::string& player_mail, const uint16_t points, const uint8_t passed_level)
{
    if( !(points <= (uint16_t)500) || !(passed_level <= (uint8_t)5) )
    {
        return errBPts;
    }
    
    //check does it exist already
    const auto check = SQL_pimpl->check_existance(player_mail.c_str(), "email" );
    if(check == SQL_enum_handler::checkRow_st::Error)
    {
        return errSrv;
    }
    else if(check == SQL_enum_handler::checkRow_st::Free)
    {
        return errBMail;
    }
    return (true == SQL_pimpl->change_players_score(player_mail, points, passed_level)) ? ok : errSrv ;
}

std::string SQL::getPlayerData(const std::string& player_username_mail)
{
    std::string data_type = "nickname";
    for(const auto& letter: player_username_mail)
    {
        if('@' == letter)
        {
            data_type = "email";
            break;
        }
    }

    auto check = SQL_pimpl->check_existance(player_username_mail.c_str(), data_type.c_str() );
    if(SQL_enum_handler::checkRow_st::Error == check) 
    {
        return errSrv;
    }

    const auto playerData = SQL_pimpl->getPlayerData(player_username_mail.c_str(), data_type.c_str() );
    if(errSrv == playerData[0] )
    {
        return errSrv;
    }
    std::string playerDataString = "";
    for(uint8_t i=0; i<playerData.size(); ++i)
    {   
        playerDataString += playerData[i];
        if(i != playerData.size()-1 )
        {
            playerDataString += ';';
        }
    }
    return playerDataString;
}

void SQL::attach(IObserver *observer) 
{
    SQL_pimpl->attach(observer);
}

void SQL::QuitSQL(void)
{
    SQL_MThandler::timerOn = false;
    SQL_MThandler::ping = true;
    SQL_MThandler::condition_var.notify_all();
}
