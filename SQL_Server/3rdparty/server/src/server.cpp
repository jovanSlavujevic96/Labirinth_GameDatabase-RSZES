#include "../include/server.h"
#include "../../sql/include/sql.h"

#include <unistd.h> 
#include <stdio.h> 
#include <sys/socket.h> 
#include <stdlib.h> 
#include <netinet/in.h> 
#include <string.h> 
#define PORT 8080 

#include <thread>
#include <iostream>
#include <vector>
#define BUFFER_SIZE 1024
#define LDB_FILENAME "test.txt"

#include <mutex>
#include <condition_variable>
#include <atomic>

namespace remove_client
{
    static std::mutex mutex;
    static std::condition_variable condition_var;
    static volatile std::atomic<bool> kill_thread(false);
    static volatile int index = -1;
};

namespace helper_func
{

static int GetSocketIndex(int socket, std::vector<int>& socket_vector)
{
    for(unsigned int i=0; i<socket_vector.size(); ++i)
    {
        if(socket_vector[i] == socket)
            return (int)i;
    }
    return -1;
}

static bool checkIfBufferEmpty(const char* buffer)
{
    for(unsigned int i=0; i<strlen(buffer); ++i)
    {
        if(buffer[i] )
            return false;
    }
    return true;
}

static void clearBuffer(char* buffer)
{
    for(unsigned int i=0; i<strlen(buffer); ++i)
    {
        if(! buffer[i] )
            return;
        buffer[i] = 0;
    }
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
        {
            break;
        }
    }
    return parameters;
}

};

class Server::ServerImpl
{
private:
    volatile struct sockaddr_in address; 
    volatile const int addrlen = sizeof(address); 
    volatile int server_fd, new_socket;
    std::vector<std::thread> client_threads;
    std::vector<int> client_sockets;
    std::thread ClRcvTH, ClRmTH;
    std::shared_ptr<SQL> sql_ptr;

    std::string chooseSQLaction(const std::vector<std::string>& parameters, bool& signed_up);
    void NewClientReceivement(void);
    void ClientCommunication(int& socket);
    void RemoveClients(void);

public:
    explicit ServerImpl() = default;
    virtual ~ServerImpl();

    bool Init(void);
    bool QuitServer(void);
    bool AssignSQL(std::shared_ptr<SQL>& sql);
};

std::string Server::ServerImpl::chooseSQLaction(const std::vector<std::string>& parameters, bool& signed_up)
{
    if("SIGN_UP" == parameters[0] && parameters.size() >= 4)
    {
        std::string res;
        signed_up = ("OK" == (res = sql_ptr->insert_new_player(
            parameters[1], parameters[2], parameters[3]) )) ? true : false;
        return res;
    }
    else if(parameters[0] == "SIGN_IN" && parameters.size() >= 3)
    {
        std::string res;
        signed_up = ("OK" == (res = sql_ptr->sign_in(
            parameters[1], parameters[2]) )) ? true : false;
        return res;
    }
    else if(parameters[0] == "CHANGE_PASS" && parameters.size() >= 4 && signed_up)
    {
        return sql_ptr->change_players_password(parameters[1], parameters[2], parameters[3]);
    }
    else if(parameters[0] == "CHANGE_NAME" && parameters.size() >= 4 && signed_up)
    {
        return sql_ptr->change_players_name(parameters[1], parameters[2], parameters[3]);
    }
    return "ERR";
}

void Server::ServerImpl::NewClientReceivement(void)
{
    while( !((new_socket = accept(server_fd, (struct sockaddr *)&address, 
					(socklen_t*)&addrlen))<0) ) 
    {
        std::cout << "new socket: " << new_socket << std::endl;
		client_sockets.push_back((int)new_socket);
		int index = helper_func::GetSocketIndex((int)new_socket, client_sockets);
		client_threads.push_back(std::thread(
            &Server::ServerImpl::ClientCommunication, 
            this, 
            std::ref(client_sockets[index]) ) );
    }
	std::cout << "NewClientReceivement :: exit\n";
}

void Server::ServerImpl::ClientCommunication(int& socket)
{
	char buffer[BUFFER_SIZE] = {0};
    int valread;
    bool signed_up = false;
    std::string msg; 
	while(true)
	{
		if( (valread = read(socket, buffer, BUFFER_SIZE) ) <= 0 || helper_func::checkIfBufferEmpty(buffer) )
		{
			goto exit;
		}
		std::cout << "Socket: " << socket << " MSG: " << buffer << '\n';
        auto params = helper_func::parseString(buffer);

        std::cout << "params[0] :: " << params[0] << '\n';
        if("GET_LDB" == params[0] && signed_up )  //send leaderboard via XML file
        {
            unsigned int numOfLines;
            if(!sql_ptr->generateXMLfile(LDB_FILENAME, numOfLines) )
            {
                goto exit;
            }
            msg = std::to_string(numOfLines) + '\n';
            if( (valread = send(socket, msg.c_str(), msg.length(), 0)) <= 0)
            {
                goto exit;
            }
            
            std::cout << "MSG sent: " << msg; //

            helper_func::clearBuffer(buffer);
            valread = read(socket, buffer, BUFFER_SIZE);
            msg = buffer;
            msg.erase(msg.find('\n'), msg.length() );

            std::cout << "MSG received: " << msg; //
            
            if(valread <= 0 || helper_func::checkIfBufferEmpty(buffer) || msg != "OK")
		    {
			    goto exit;
		    }

            FILE *fd = fopen(LDB_FILENAME, "rb");
            int bytes_read;
            while (!feof(fd)) 
            {
                if ((bytes_read = fread(&buffer, 1, BUFFER_SIZE, fd)) > 0)
                {
                    if( (valread = send(new_socket, buffer, bytes_read, 0)) <= 0)
                    {
                        goto exit;
                    }
                }
                else
                    break;
	        }
            std::cout << "Socket: " << socket << " FILE sent\n";
            fclose(fd);
        }
        else if(params[0] != "GET_LDB")
        {
            msg = Server::ServerImpl::chooseSQLaction(params, signed_up);
        }

        if(params[0] != "GET_LDB")
        {
            msg += '\n';
            std::cout << "Socket: " << socket  << " sent MSG: " << msg << " succesfully\n"; //
            if( (valread = send(socket, msg.c_str(), msg.length(), 0)) <= 0)
            {
                goto exit;
            }
        }
		helper_func::clearBuffer(buffer);
	}
    exit:
    {
        std::cout << "ClientCommunication :: exit\n";
        remove_client::index = helper_func::GetSocketIndex(socket, client_sockets);
        remove_client::condition_var.notify_one();
        return;
    }
}

void Server::ServerImpl::RemoveClients(void)
{
    while(true)
    {
        std::unique_lock<std::mutex> lk(remove_client::mutex);
        remove_client::condition_var.wait(lk, [&]{return (remove_client::index != -1 || remove_client::kill_thread == true); });

		if(static_cast<bool>(remove_client::kill_thread) == true)
			break;

        client_threads[remove_client::index].join();
        client_threads.erase(client_threads.begin()+remove_client::index);
		client_sockets.erase(client_sockets.begin()+remove_client::index);

        remove_client::index = -1;
    }
	std::cout << "RemoveClients :: exit\n";
}

Server::ServerImpl::~ServerImpl()
{
    for(auto& thread : client_threads)
    {
        //thread.join();
        thread.detach();
    }
    //ClRcvTH.join();
    ClRcvTH.detach();
    //ClRmTH.join();
    ClRmTH.detach();
}

bool Server::ServerImpl::Init(void)
{
    	const int opt = 1;

	// Creating socket file descriptor 
	if((server_fd = socket(AF_INET, SOCK_STREAM, 0)) == 0) 
	{ 
		perror("socket failed"); 
		return false;//exit(EXIT_FAILURE); 
	} 
	
	// Forcefully attaching socket to the port 8080 
	if (setsockopt(server_fd, SOL_SOCKET, SO_REUSEADDR, &opt, sizeof(opt))) 
	{ 
		perror("setsockopt"); 
		return false;//exit(EXIT_FAILURE); 
	} 
	address.sin_family = AF_INET; 
	address.sin_addr.s_addr = INADDR_ANY; 
	address.sin_port = htons( PORT ); 
	
	// Forcefully attaching socket to the port 8080 
	if (bind(server_fd, (struct sockaddr *)&address, sizeof(address))<0) 
	{ 
		perror("bind failed"); 
		return false;//exit(EXIT_FAILURE); 
	} 
	if (listen(server_fd, 3) < 0) 
	{ 
		perror("listen"); 
        return false;//exit(EXIT_FAILURE); 
	}

    	ClRcvTH = std::thread(&Server::ServerImpl::NewClientReceivement, this);
	ClRmTH = std::thread(&Server::ServerImpl::RemoveClients, this);

    return true;
}

bool Server::ServerImpl::QuitServer(void)
{
    remove_client::kill_thread = true;
    shutdown(new_socket, SHUT_RDWR);
    close(new_socket);
    shutdown(server_fd, SHUT_RDWR);
    close(server_fd);
    for(auto& socket : client_sockets)
    {
        shutdown(socket, SHUT_RDWR);
        close(socket);
    }
    remove_client::condition_var.notify_one();
    return true;
}

bool Server::ServerImpl::AssignSQL(std::shared_ptr<SQL>& sql)
{
    if(sql_ptr.get() != nullptr) 
        return true;

    this->sql_ptr = sql;
    return (sql_ptr.get() != nullptr) ? true : false;
}

Server::Server() :
    srv_pimpl{ std::make_unique<ServerImpl>() }
{

}

Server::~Server() = default;

extern "C" bool Server::RunServer(void)
{
    static bool alreadyInitialised = false;
    
    if(true == alreadyInitialised)
        return false;
    
    return ( (alreadyInitialised = srv_pimpl->Init() ) ); 
}

extern "C" bool Server::QuitServer(void)
{
    static bool alreadyQuited = false;

    if(true == alreadyQuited)
        return false;

    return ( (alreadyQuited = srv_pimpl->QuitServer() ) ); 
}

extern "C" bool Server::AssignSQL(std::shared_ptr<SQL>& sql)
{
    static bool alreadyAssigned = false;

    if(true == alreadyAssigned)
        return false;

    return ( (alreadyAssigned = srv_pimpl->AssignSQL(sql) ));
}
