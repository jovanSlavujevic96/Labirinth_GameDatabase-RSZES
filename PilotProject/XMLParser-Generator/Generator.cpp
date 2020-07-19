#include <tinyxml.h>
#include "../../SQL_Server/3rdparty/sql/include/Data.h"
#include <iostream>

bool generateXML(const Data* data)
{
    if(nullptr == data)
    {
        return false;
    }
    if(nullptr == data->level || nullptr == data->name || nullptr == data->points)
    {
        return false;
    }
    TiXmlDocument doc; 
    doc.LinkEndChild( new TiXmlDeclaration("1.0", "", "") );
    TiXmlElement * root = new TiXmlElement( "leaderboard" );
    for(uint8_t i=0; i<(*data->name).size(); ++i)
    {
        TiXmlElement * child = new TiXmlElement( "player" );  
        child->SetAttribute("points", (*data->points)[i] );

        {
            TiXmlElement * element = new TiXmlElement( "name" );
            TiXmlText * text = new TiXmlText( (*data->name)[i] );
	        element->LinkEndChild( text );
            child->LinkEndChild(std::move(element) );
        }
        {
            TiXmlElement * element = new TiXmlElement( "level" );
            TiXmlText * text = new TiXmlText( (*data->level)[i] );
	        element->LinkEndChild( text );
            child->LinkEndChild(std::move(element) );
        }
        
        root->LinkEndChild(std::move(child) );
    }
    doc.LinkEndChild(std::move(root) );
    auto DOC = doc.SaveFile(data->FILENAME);
    return DOC;
}

int main()
{
    Data data;
    data.FILENAME = "leaderboard.xml";

    std::vector<std::string> name, lvl, pts;
    auto size = 8;
    ///*
    name.reserve(size);
    lvl.reserve(size);
    pts.reserve(size);
    //*/
    auto i=size;
    while(i--)
    {
        if(i%2)
        {
            name.emplace_back("Jovan");
            lvl.emplace_back("4");
            pts.emplace_back("320");
        }
        else
        {
            name.emplace_back("Stevan");
            lvl.emplace_back("3");
            pts.emplace_back("270");
        }
        
    }

    data.level = &lvl;
    data.points = &pts;
    data.name = &name;

    //std::cout << (*data.level)[1] << '\n';
    
    if(generateXML(&data) ) std::cout << "OK\n";  
    return 0;
}