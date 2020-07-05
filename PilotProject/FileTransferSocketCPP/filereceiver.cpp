// Client side C/C++ program to demonstrate Socket programming 
#include <stdio.h> 
#include <sys/socket.h> 
#include <arpa/inet.h> 
#include <unistd.h> 
#include <string.h> 

#include <iostream>
#include <string>

#define PORT 8080 
#define BUFFER_SIZE 1024

void clearBuffer(char* buffer);

int main(int argc, char const *argv[]) 
{ 
	int sock = 0, valread; 
	struct sockaddr_in serv_addr; 
	const std::string hello = "Hello from client"; 
	char buffer[1024] = {0}; 
	if ((sock = socket(AF_INET, SOCK_STREAM, 0)) < 0) 
	{ 
		printf("\n Socket creation error \n"); 
		return -1; 
	} 

	serv_addr.sin_family = AF_INET; 
	serv_addr.sin_port = htons(PORT); 
	
	// Convert IPv4 and IPv6 addresses from text to binary form 
	if(inet_pton(AF_INET, "127.0.0.1", &serv_addr.sin_addr)<=0) 
	{ 
		printf("\nInvalid address/ Address not supported \n"); 
		return -1; 
	} 

	if (connect(sock, (struct sockaddr *)&serv_addr, sizeof(serv_addr)) < 0) 
	{ 
		printf("\nConnection Failed \n"); 
		return -1; 
	} 
	size_t datasize;
    FILE* fd = fopen("rcv.txt", "wb");
    char text[BUFFER_SIZE] = {0};
    while (true)
    {
        datasize = recv(sock, text, sizeof(text), 0);
        if(!datasize)
        {
            std::cout << "Im out of while loop\n";
            break;
        }
        fwrite(&text, 1, datasize, fd);
        clearBuffer(text);
    }
	
    send(sock , hello.c_str() , hello.length() , 0 );
    {
	    std::string hello_output = ("Hello message sent\n"); 
        std::cout << hello_output;
    }
    fclose(fd);
    return 0; 
} 

void clearBuffer(char* buffer)
{
    for(int i=0; i<strlen(buffer); ++i)
    {
        buffer[i] = 0;
    }
}
