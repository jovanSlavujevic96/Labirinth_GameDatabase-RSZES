#include <server.hpp>

#include <unistd.h> 
#include <stdio.h> 
#include <sys/socket.h> 
#include <stdlib.h> 
#include <netinet/in.h> 

#include <string.h>
#include <string>

#include <thread>
#include <mutex>
#include <condition_variable>

#define PORT 8080
#define BUFFER_SIZE 1024

class Server::ServerImpl
{
private:
    struct sockaddr_in address;
    static int8_t server_fd;
    const uint8_t addrlen;

    std::vector<player_s> players;
    std::unique_ptr<SQL> sql;

    std::vector<int64_t> sockets;
    std::vector<std::thread> threads;

    uint64_t FindSocketsIndex(const int64_t socket);
    uint64_t FindPlayerIndex(const player_s* player);
    bool ServerInit(void);
    bool chooseSQLaction(const std::vector<std::string>& parameters, player_s* player, bool& signed_up);
    void CommunicationWithClient(const int64_t socket);

public:
    ServerImpl(std::unique_ptr<SQL>& sql);
    virtual ~ServerImpl();

    void NewClientReceivement(void);
};

int8_t Server::ServerImpl::server_fd = 0;

uint64_t Server::ServerImpl::FindSocketsIndex(const int64_t socket)
{
    for(uint64_t index=0; index < sockets.size(); ++index)
    {
        if(socket == sockets[index])
        {
            return index;
        }
    }
    return sockets.size()+1;
}

bool Server::ServerImpl::ServerInit(void)
{
    const uint8_t opt = 1; 

    // Creating socket file descriptor 
    if ((server_fd = socket(AF_INET, SOCK_STREAM, 0)) == 0) 
    { 
        perror("socket failed"); 
        return false; 
    }

    // Forcefully attaching socket to the port 8080 
    if (setsockopt(server_fd, SOL_SOCKET, SO_REUSEADDR | SO_REUSEPORT, &opt, sizeof(int))) 
    { 
        perror("setsockopt"); 
        return false; 
    }
    address.sin_family = AF_INET; 
    address.sin_addr.s_addr = INADDR_ANY; 
    address.sin_port = htons( PORT );

    // Forcefully attaching socket to the port 8080 
    if (bind(server_fd, (struct sockaddr *)&address, sizeof(address))<0) 
    { 
        perror("bind failed"); 
        return false; 
    } 
    if (listen(server_fd, 3) < 0) 
    { 
        perror("listen"); 
        return false;
    }
    return true;   
}

static void resetBuffer(char* buffer)
{
    for(uint16_t i=0; i<BUFFER_SIZE; ++i)
    {
        if(!buffer[i])
        {
            return;
        }
        buffer[i] = 0;
    }
}

static bool isBufferEmpty(const char* buffer)
{
    for(uint16_t i=0; i<BUFFER_SIZE; ++i)
    {
        if(buffer[i])
        {
            return false;
        }
    }
    return true;
}

static std::vector<std::string> parseString(const char* buffer)
{
    std::vector<std::string> parameters;
    parameters.push_back(std::string() );
    uint8_t index=0;
    for(uint16_t i=0; i<1024; ++i)
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
            break;
    }
    return parameters;
}

bool Server::ServerImpl::chooseSQLaction(const std::vector<std::string>& parameters, player_s* player, bool& signed_up)
{
    if(parameters[0] == "SIGN_IN")
    {
        signed_up = sql->sign_in(player);
        return signed_up;
    }
    else if(parameters[0] == "CHANGE_PASS" && parameters.size() > 4)
    {
        return sql->change_players_password(player, parameters[4].c_str() );
    }
    else if(parameters[0] == "CHANGE_NAME" && parameters.size() > 4)
    {
        return sql->change_players_name(player, parameters[4].c_str() );
    }
    else
    {
        return false;
    }
}

uint64_t Server::ServerImpl::FindPlayerIndex(const player_s* player)
{
    uint64_t index;
    for(auto&& vecPlayer : players)
    {
        if(vecPlayer.getNickname() == player->getNickname() && \
           vecPlayer.getEmail() == player->getEmail() )
        {
            return index;
        }
        index++;
    }
    return players.size()+1;
}

void Server::ServerImpl::CommunicationWithClient(const int64_t socket)
{
    char buffer[BUFFER_SIZE] = {0};
    player_s* player_ptr;
    bool signed_up = false;
    uint64_t index, pl_index;
    
    std::mutex m;
    std::condition_variable cv;

    while(true)
    {
        index = FindSocketsIndex(socket);            
        auto ret = read(socket, buffer, BUFFER_SIZE);
        if(isBufferEmpty(buffer) || !ret || index > sockets.size() )
        {
            goto exit;
        }
        else
        {
            auto params = parseString(buffer);
            bool info;
            if(!strcmp(params[0].c_str(), "SIGNUP" ) && !signed_up )
            {
                player_s tmp_player(params[1], params[2], params[3]);
                tmp_player.setSocket(socket);
                info = sql->insert_new_player(&tmp_player);
                if(info)
                {
                    players.push_back(tmp_player);
                    pl_index = FindPlayerIndex(&tmp_player);
                    if(!(pl_index > players.size() ) )
                    {
                        player_ptr = &players[pl_index];
                        signed_up = true;
                    }
                    else
                    {
                        goto exit;
                    }
                }
            }
            else
                info = Server::ServerImpl::chooseSQLaction(params, player_ptr, signed_up);

            if(info)
                (void) send(socket, "OK", 2, 0); 
            else
                (void) send(socket, "ERR", 3, 0);     
        }
        resetBuffer(buffer);
    }
    exit:
    {
        sockets.erase(sockets.begin()+index);
        if(pl_index <= players.size() && players.size() )
            players.erase(players.begin()+pl_index);
        cv.notify_all();
        return;
    }
    
}

Server::ServerImpl::ServerImpl(std::unique_ptr<SQL>& sql) :
    addrlen{sizeof(address)},
    sql{std::move(sql)}
{
    bool ret = Server::ServerImpl::ServerInit();
    if(!ret)
    {
        std::exit(-1);
    }
}

Server::ServerImpl::~ServerImpl() 
{
    for(auto &&thread : threads)
    {
        thread.join();
    }
}

void Server::ServerImpl::NewClientReceivement(void)
{
    while(true)
    {
        int64_t new_socket;
        if( !( (new_socket = accept(server_fd, (struct sockaddr *)&address, (socklen_t*)&addrlen) )<0 ) )
        {
            sockets.push_back(new_socket);
            auto index = FindSocketsIndex(new_socket);
            if(index >= threads.size() )
            {
                threads.push_back(std::thread(&Server::ServerImpl::CommunicationWithClient, this, new_socket));
                threads[threads.size()-1].join();
            }
            else
            {
                threads[index] = std::thread(&Server::ServerImpl::CommunicationWithClient, this, new_socket);
                threads[index].join();
            }
        }
    }
}

Server::Server(std::unique_ptr<SQL>& sql) :
    ServerPimpl{std::make_unique<ServerImpl>(sql) }
{

}

Server::~Server() = default;

void Server::ClientCommunication(void)
{
    ServerPimpl->NewClientReceivement();
}