#ifndef MYSQLCPP_INCLUDE_KEYBOARD_HPP_
#define MYSQLCPP_INCLUDE_KEYBOARD_HPP_

typedef enum {Normal, Hidden} keyboard_st;
void SetKeyboard(const keyboard_st state);

#endif //MYSQLCPP_INCLUDE_KEYBOARD_HPP_

