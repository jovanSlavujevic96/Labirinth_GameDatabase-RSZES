#include <keyboard.hpp>

#include <termios.h>
#include <unistd.h>

void SetKeyboard(const keyboard_st state)
{
    static struct termios kbd;
    tcgetattr(STDIN_FILENO, &kbd);

    if(state == Hidden)
    {
        /* disable echo */
        kbd.c_lflag &= ~ECHO;
    }
    else if(state == Normal)
    {
        /* reenable echo */
        kbd.c_lflag |= ECHO;
    }
    tcsetattr(STDIN_FILENO, TCSANOW, &kbd);
}
