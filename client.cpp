#include <stdio.h> 
#include <sys/socket.h> 
#include <arpa/inet.h> 
#include <unistd.h> 
#include <string.h>
#define PORT 8080 
#include <termios.h>
#include <unistd.h>
#include <iostream>
#include <string>
#include <sstream>

#define BUFFER_SIZE 1024

typedef enum {KeyboardOld_,KeyboardNew_} KeyboardState_t;

void SetKeyboard(const KeyboardState_t state)
{
    static struct termios oldt, newt;
    tcgetattr( STDIN_FILENO, &oldt);
    newt = oldt;
    newt.c_lflag &= ~(ICANON);
    auto keyboard = (state == KeyboardOld_) ? oldt : newt;      
    tcsetattr( STDIN_FILENO, TCSANOW, &keyboard);
}
   
bool IsCharSomeOfAllowed(const char character, const char* letters="aAsSdDwW")
{
    for(int i=0; i<strlen(letters); ++i)
    {
        if(character == letters[i])
            return true;
    }
    return false;
}

std::string generateMessage(const char CHARACTER)
{
    static bool signed_up = false;
    SetKeyboard(KeyboardOld_);   
	std::string tmp;
	std::string email, nickname, password;
    std::string output="";
	if(CHARACTER == 'a' || CHARACTER == 'A')
	{
		std::cout << "\nYou are trying to sign in!\nInsert email, nickname and password!\n";
		std::cin >> email >> nickname >> password;
		output = ("SIGN_IN\n" + email + '\n' + nickname + '\n' + password);
        signed_up = true;
	}
	else if(CHARACTER == 's' || CHARACTER == 'S')
	{
		std::string new_password;
		std::cout << "\nYou are trying to change password!\nInsert email, nickname, old and new password!\n";
		std::cin >> email >> nickname >> password >> new_password;
		output = ("CHANGE_PASS\n" + email + '\n' + nickname + '\n' + password + '\n' + new_password);
	}
	else if(CHARACTER == 'd' || CHARACTER == 'D')
	{
		std::string new_name;
		std::cout << "\nYou are trying to change name!\nInsert email, old name, password and new_name!\n";
		std::cin >> email >> nickname >> password >> new_name;
		output = ("CHANGE_NAME\n" + email + '\n' + nickname + '\n' + password + '\n' + new_name);
	}
    else if( (CHARACTER == 'w' || CHARACTER == 'W') && !signed_up)
    {
		std::cout << "\nYou are trying to sign up!\nInsert email, name and password!\n";
		std::cin >> email >> nickname >> password;
		output = ("SIGN_UP\n" + email + '\n' + nickname + '\n' + password);
        signed_up = true;
    }
    SetKeyboard(KeyboardNew_);
	return output;
}

bool ProtocolInit(int& socket_, const char* IP="127.0.0.1")
{
    struct sockaddr_in serv_addr;
    if ((socket_ = socket(AF_INET, SOCK_STREAM, 0)) < 0) 
    { 
        printf("\n Socket creation error \n"); 
        return false; 
    }
    serv_addr.sin_family = AF_INET; 
    serv_addr.sin_port = htons(PORT);

    // Convert IPv4 and IPv6 addresses from text to binary form 
    if(inet_pton(AF_INET, IP, &serv_addr.sin_addr)<=0)  
    { 
        printf("\nInvalid address/ Address not supported \n"); 
        return false; 
    }

    std::cout << "Pending connection with server..\n";
    while (connect(socket_, (struct sockaddr *)&serv_addr, sizeof(serv_addr)) < 0) ;
	std::cout << "Succesfully connected to the server..\n";

    return true;    
}

void resetBuffer(char* buffer)
{
    for(int i=0; i<BUFFER_SIZE; ++i)
    {
        if(!buffer[i])
        {
            return;
        }
        buffer[i] = 0;
    }
}

bool checkIfEmpty(const char* buffer)
{
    for(int i=0; i<BUFFER_SIZE; ++i)
    {
        if(buffer[i])
        {
            return false;
        }
    }
    return true;
}


int main(int argc, char *argv[]) 
{ 
	SetKeyboard(KeyboardNew_);
    int sock = 0; 
  
    if(argv[1])
	{
		if(!ProtocolInit(sock,argv[1]))
        	return -1;
	}
	else
	{
		if(!ProtocolInit(sock))
        	return -1;
	}
	
    char buffer[BUFFER_SIZE] = {0};
    while(true)
    {
        int c = getchar();
        if(IsCharSomeOfAllowed(c) == true)
		{
			auto msg = generateMessage(c);
		    send(sock, msg.c_str(), msg.length(), 0); 
			std::cout << "\nMSG: \"" << msg << "\" succesfully sent\n"; 
			auto valread = read(sock, buffer, BUFFER_SIZE );
            if(checkIfEmpty(buffer) || valread <= 0 )
            {
                std::cout << "Server disconnected\n";
                break;
            }
            else
            {
                std::cout << "MSG received: \"" << buffer << "\"\n";
            }
		}
		else if(c == 'q' || c == 'Q')
        {
            std::cout << " pressed\n"; 
            break;
        }
        resetBuffer(buffer);
    }
    SetKeyboard(KeyboardOld_);
    return 0; 
} 