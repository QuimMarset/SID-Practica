#!/bin/bash

echo "Compilando acciones y conceptos"
ficheros=$(find src/Ontologia/*.java);
for i in $ficheros;do
   echo $i
   var=$var" "$i 
done;

echo "Hecho"

echo "Compilando Agentes"

ficheros=$(find src/Agentes/*.java);
for i in $ficheros;do
    echo $i
    agents=$agents" "$i
done;

echo "Hecho"

export CLASSPATH="$CLASSPATH:jade/lib/jade.jar:jade/lib/commons-codec/commons-codec-1.3.jar:jade/classes"

javac -nowarn -Xlint:-unchecked -cp jade/lib/jade.jar -d jade/classes $var $agents

echo
echo "Inserta numero de industrias:"
read num

echo "(1) Todas con la misma capacidad"
echo "(2) Insertar capacidad manualmente"
read opt

if [ $opt = 2 ];
then
    for ((i=1; i<=$num; i++)); do
    echo "Inserte capacidad del tanque de la industria "$i
    read c
        if [ $i = 1 ];
        then 
            industria="Industria"$i":Agentes.Industria("$c",Rio,EDAR,Lluvia)"
            aid="Industria"$i
            sniff="Industria"$i
            capacidades=$c
        else
            industria=$industria";Industria"$i":Agentes.Industria("$c",Rio,EDAR,Lluvia)"
            aid=$aid",Industria"$i
            sniff=$sniff";Industria"$i
            capacidades=$capacidades","$c
        fi
    done;
else
    echo "Inserte capacidad:"
    read capacidad
    for ((i=1; i<=$num; i++)); do
        if [ $i = 1 ];
        then
             industria="Industria"$i":Agentes.Industria("$capacidad",Rio,EDAR,Lluvia)"
             aid="Industria"$i
             sniff="Industria"$i
             capacidades=$c
        else
             industria=$industria";Industria"$i":Agentes.Industria("$capacidad",Rio,EDAR,Lluvia)"
             aid=$aid",Industria"$i
             sniff=$sniff";Industria"$i
             capacidades=$capacidades","$c
        fi
    done;
fi

echo "java jade.Boot -gui -agents"$industria";Rio:Agentes.Rio("$capacidades");EDAR:Agentes.EDAR("$aid",Rio,Lluvia);Lluvia:Agentes.Lluvia("$aid",Rio,EDAR);snif:jade.tools.sniffer.Sniffer"

rm *.properties
echo "preload="$sniff";Rio;EDAR;Lluvia" >> sniffer.properties

java jade.Boot -gui -agents $industria";Rio:Agentes.Rio;EDAR:Agentes.EDAR("$aid",Rio,Lluvia);Lluvia:Agentes.Lluvia("$aid",Rio,EDAR);snif:jade.tools.sniffer.Sniffer"

echo




