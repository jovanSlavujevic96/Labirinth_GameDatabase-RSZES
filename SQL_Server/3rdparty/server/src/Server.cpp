#include "../include/Server.h"
#include "../../sql/include/SQL.h"

//C libs
#include <unistd.h> 
#include <stdio.h> 
#include <sys/socket.h> 
#include <stdlib.h> 
#include <netinet/in.h> 
#include <string.h> 

//STL CPP libs
#include <iostream>
#include <thread>
#include <vector>
#include <map>
#include <mutex>
#include <condition_variable>
#include <atomic>
#include <fstream>  

#define PORT 8080 
#define BUFFER_SIZE 1024

namespace Server_MThandler
{
    static std::mutex mutex;
    static std::condition_variable condition_var;
    static volatile std::atomic<bool> kill_thread(false), send_data{false};
    static volatile int32_t index = -1;
};

namespace helper_func
{
    static inline bool checkIsBufferEmpty(const char* buffer);
    static inline void clearBuffer(char* buffer);
    static inline std::vector<std::string> parseString(const char* buffer);
};

/*
 * helper_func definition
 */
static inline bool helper_func::checkIsBufferEmpty(const char* buffer)
{
    for(uint16_t i=0; i<strlen(buffer); ++i)
    {
        if(buffer[i] )
        {
            return false;
        }
    }
    return true;
}

static inline void helper_func::clearBuffer(char* buffer)
{
    for(uint16_t i=0; i<BUFFER_SIZE; ++i)
    {
        if(! buffer[i] )
        {
            return;
        }
        buffer[i] = 0;
    }
}

static inline std::vector<std::string> helper_func::parseString(const char* buffer)
{
    std::vector<std::string> parameters;
    parameters.push_back(std::string() );
    uint8_t index=0;
    for(uint16_t i=0; i<strlen(buffer); ++i)
    {
        if(buffer[i] != '\n' && buffer[i])
        {
            parameters[index] += buffer[i];
        }
        else if(buffer[i] == '\n')
        {
            parameters.push_back(std::string() );
            index++;
        }
        else if(!buffer[i])
        {
            break;
        }
    }
    return parameters;
}

/*
 * class ServerImpl definition
 */
class Server::ServerImpl
{
private:
    struct sockaddr_in m_address; 
    volatile int16_t m_serverFd, m_newSocket;

    std::vector<std::string> m_fileContent;
    std::thread m_clientRcvTH, m_clientRmTH;
    std::unique_ptr<SQL> m_sqlPtr;
    std::vector<std::unique_ptr<Server::Socket>> m_sockets;

    void NewClientReceivement(void);
    void RemoveClients(void);

public:
    explicit ServerImpl() = default;
    virtual ~ServerImpl();

    bool Init(void);
    bool QuitServer(void);
    bool AssignSQL(std::unique_ptr<SQL> sql);
    void Update(const Data& notification);

    std::string chooseSQLaction(const std::vector<std::string>& parameters, bool& signed_up);
    int32_t getSocketIndexVal(Server::Socket* socket);
    const std::vector<std::string>& getFileContent() const;
};

/*
 * class Socket definition
 */
class Server::Socket
{
    int16_t m_socketValue;
    std::thread m_worker;
    bool m_upToDate;
    Server::ServerImpl* m_server;

    void clientCommunication();

public:
    Socket(int16_t socket, Server::ServerImpl* server);
    ~Socket() = default;

    void doUpdate();
    int16_t getSocketValue() const;
    std::thread& getSocketThread();
};

/*
 * class ServerImpl PRIVATE METHODS definition
 */
void Server::ServerImpl::NewClientReceivement(void)
{
    auto addrlen = sizeof(m_address); 
    while( !((m_newSocket = accept(m_serverFd, (struct sockaddr *)&m_address, 
                    (socklen_t*)&addrlen)) <= 0) ) 
    {
        std::cout << "new socket: " << m_newSocket << '\n';
        m_sockets.push_back( std::make_unique<Server::Socket>(m_newSocket, this) );
    }
	std::cout << "NewClientReceivement :: exit\n";
}

void Server::ServerImpl::RemoveClients(void)
{
    while(true)
    {
        std::unique_lock<std::mutex> lk(Server_MThandler::mutex);
        Server_MThandler::condition_var.wait(lk, [&]{return (Server_MThandler::index != -1 || Server_MThandler::kill_thread == true); });

		if(static_cast<bool>(Server_MThandler::kill_thread) == true)
			break;

        m_sockets[Server_MThandler::index]->getSocketThread().join();
		m_sockets.erase(m_sockets.begin()+Server_MThandler::index);

        Server_MThandler::index = -1;
    }
	std::cout << "RemoveClients :: exit\n";
}

/*
 * class ServerImpl PUBLIC METHODS definition
 */
Server::ServerImpl::~ServerImpl()
{
    m_clientRcvTH.detach();
    m_clientRmTH.detach();
}

bool Server::ServerImpl::Init(void)
{
    const uint8_t opt = 1;

	// Creating socket file descriptor 
	if((m_serverFd = socket(AF_INET, SOCK_STREAM, 0)) == 0) 
	{ 
		perror("socket failed"); 
		return false;
	} 
	// Forcefully attaching socket to the port 8080 
	if (setsockopt(m_serverFd, SOL_SOCKET, SO_REUSEADDR, &opt, sizeof(int64_t))) 
	{ 
		perror("setsockopt"); 
		return false;
	} 
	m_address.sin_family = AF_INET; 
	m_address.sin_addr.s_addr = INADDR_ANY; 
	m_address.sin_port = htons( PORT ); 
	
	// Forcefully attaching socket to the port 8080 
	if (bind(m_serverFd, (struct sockaddr *)&m_address, sizeof(m_address))<0) 
	{ 
		perror("bind failed"); 
		return false; 
	} 
	if (listen(m_serverFd, 3) < 0) 
	{ 
		perror("listen"); 
        return false; 
	}
    m_clientRcvTH = std::thread(&Server::ServerImpl::NewClientReceivement, this);
	m_clientRmTH = std::thread(&Server::ServerImpl::RemoveClients, this);

    return true;
}

bool Server::ServerImpl::QuitServer(void)
{
    Server_MThandler::kill_thread = true;

    shutdown(m_newSocket, SHUT_RDWR);
    close(m_newSocket);
    
    shutdown(m_serverFd, SHUT_RDWR);
    close(m_serverFd);
    
    for(auto& socket : m_sockets)
    {
        shutdown(socket->getSocketValue(), SHUT_RDWR);
        close(socket->getSocketValue() );
        socket->getSocketThread().detach();
    } 
    Server_MThandler::condition_var.notify_one();

    if(m_sqlPtr.get() != nullptr)
    {
        m_sqlPtr->QuitSQL();
    }
    return true;
}

bool Server::ServerImpl::AssignSQL(std::unique_ptr<SQL> sql)
{
    if(m_sqlPtr.get() != nullptr) 
    {
        return true;
    }
    this->m_sqlPtr = std::move(sql);
    return (m_sqlPtr.get() != nullptr) ? true : false;
}

void Server::ServerImpl::Update(const Data& notification)
{
    Server_MThandler::send_data = false;
    m_fileContent.clear();
    {
        std::ifstream inFile(notification.FILENAME);
        std::string line;
        while(std::getline(inFile, line) )
        {
            m_fileContent.push_back(line + '\n');
        }
        inFile.close();
    }

    for(const auto& socket : m_sockets)
    {
        socket->doUpdate();
    }

    Server_MThandler::send_data = true; 
    Server_MThandler::condition_var.notify_all();
}

std::string Server::ServerImpl::chooseSQLaction(const std::vector<std::string>& parameters, bool& signed_up)
{
    if("SIGN_UP" == parameters[0] && parameters.size() >= 4)
    {
        std::string res;
        signed_up = ("OK" == (res = m_sqlPtr->insert_new_player(
            parameters[1], parameters[2], parameters[3]) )) ? true : false;
        return res;
    }
    else if(parameters[0] == "SIGN_IN" && parameters.size() >= 3)
    {
        std::string res;
        signed_up = ("OK" == (res = m_sqlPtr->sign_in(
            parameters[1], parameters[2]) )) ? true : false;
        return res;
    }
    else if(parameters[0] == "CHANGE_PASS" && parameters.size() >= 4 && signed_up)
    {
        return m_sqlPtr->change_players_password(parameters[1], parameters[2], parameters[3]);
    }
    else if(parameters[0] == "CHANGE_MAIL" && parameters.size() >= 4 && signed_up)
    {
        return m_sqlPtr->change_players_email(parameters[1], parameters[2], parameters[3]);
    }
    else if(parameters[0] == "CHANGE_NAME" && parameters.size() >= 4 && signed_up)
    {
        std::cout << "parameters: " << parameters[1] << ' ' << parameters[2] << ' ' << parameters[3] << '\n';
        return m_sqlPtr->change_players_name(parameters[1], parameters[2], parameters[3]);
    }
    else if(parameters[0] == "GET_INFO" && parameters.size() >= 2 && signed_up)
    {
        return m_sqlPtr->getPlayerData(parameters[1] );
    }
    else if(parameters[0] == "UPDATE_SCORE" && parameters.size() >= 4 && signed_up)
    {
        return m_sqlPtr->change_players_score(parameters[1],
            static_cast<uint16_t>(std::stoi(parameters[2])),
            static_cast<uint8_t>(std::stoi(parameters[3])) );
    }
    return "ERR";
}

int32_t Server::ServerImpl::getSocketIndexVal(Server::Socket* socket)
{
    if(nullptr == socket)
    {
        return -1;
    }
    for(uint16_t i=0; i<m_sockets.size(); ++i)
    {
        if(socket->getSocketValue() == m_sockets[i]->getSocketValue() )
        {
            return static_cast<int32_t>(i);
        }
    }
    return -1;
} 

const std::vector<std::string>& Server::ServerImpl::getFileContent() const
{
    return m_fileContent;
}
    
/*
 * class Socket PRIVATE METHODS definition
 */
void Server::Socket::clientCommunication()
{
    char buffer[BUFFER_SIZE] = {0};
    bool signed_up = false;
    std::string msg;

    while(true)
	{
        if( read(m_socketValue, buffer, BUFFER_SIZE) <= 0 || helper_func::checkIsBufferEmpty(buffer) )
		{
			break;
		}        
        auto params = helper_func::parseString(buffer);
        std::cout << "Server::Socket::clientCommunication :: socket(" << m_socketValue << ") => params[0]: " << params[0] << '\n';
        helper_func::clearBuffer(buffer);

        if("GET_LDB" == params[0] && signed_up )  //send leaderboard via XML file
        {
            if(true == m_upToDate)
            {
                msg = "UP_TO_DATE\n";
                std::cout << "sent: " << msg;
                if( send(m_socketValue, msg.c_str(), msg.length(), 0) <= 0)
                {
                    break;
                }
                continue;
            }      
            std::unique_lock<std::mutex> lk(Server_MThandler::mutex);
            Server_MThandler::condition_var.wait(lk, [&]{return (true == static_cast<bool>(Server_MThandler::send_data) ); });

            for(const auto& file_buff : m_server->getFileContent() ) 
            {
                std::cout << file_buff;
                if( send(m_socketValue, file_buff.c_str(), file_buff.length(), 0) <= 0)
                {
                    break;
                }
            }

            std::cout << "FILE SENT\n";
            m_upToDate = true;
            continue;    
        }
        msg = m_server->chooseSQLaction(params, signed_up) + '\n';
        if( send(m_socketValue, msg.c_str(), msg.length(), 0) <= 0)
        {
            break;
        }
    }
    Server_MThandler::index = m_server->getSocketIndexVal(this);
    Server_MThandler::condition_var.notify_one();
}

/*
 * class Socket PUBLIC METHODS definition
 */
Server::Socket::Socket(int16_t socket, Server::ServerImpl* server) :
    m_socketValue{socket},
    m_server{server}
{
    m_worker = std::thread(&Server::Socket::clientCommunication, this);
    m_upToDate = false;
}

void Server::Socket::doUpdate()
{
    m_upToDate = false;
}

int16_t Server::Socket::getSocketValue() const
{
    return m_socketValue;
} 

std::thread& Server::Socket::getSocketThread()
{
    return m_worker;
}

/*
 * class Server PUBLIC METHODS definition
 */
Server::Server() :
    srv_pimpl{ std::make_unique<ServerImpl>() }
{

}

Server::~Server() = default;

extern "C" bool Server::RunServer(void)
{
    static bool alreadyInitialised = false;
    
    if(true == alreadyInitialised)
    {
        return false;
    }
    return ( (alreadyInitialised = srv_pimpl->Init() ) ); 
}

extern "C" bool Server::QuitServer(void)
{
    static bool alreadyQuited = false;

    if(true == alreadyQuited)
    {
        return false;
    }
    return ( (alreadyQuited = srv_pimpl->QuitServer() ) ); 
}

extern "C" bool Server::AssignSQL(std::unique_ptr<SQL> sql)
{
    static bool alreadyAssigned = false;

    if(true == alreadyAssigned)
    {
        return false;
    }
    return ( (alreadyAssigned = srv_pimpl->AssignSQL(std::move(sql)) ));
}

extern "C" void Server::Update(const Data& notification)
{
    srv_pimpl->Update(notification);
}

