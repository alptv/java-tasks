set out=%cd%\_build
set code=%cd%\*.java

echo %out%

cd ..\..\..\..\..\..\..\
set cp=.\java-advanced-2020\modules\info.kgeorgiy.java.advanced.implementor
set mp=.\java-advanced-2020\lib\

javac -d %out% -cp %cp% -p %mp% %code%

cd %out% || exit

jar --create --main-class=ru.ifmo.rain.laptev.implementor.JarImplementor --file=..\_implemetor.jar .\ru