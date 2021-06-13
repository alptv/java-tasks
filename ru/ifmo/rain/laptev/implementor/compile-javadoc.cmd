set code=%cd%\*.java
set out=%cd%\_javadoc

cd ..\..\..\..\..\..\..\

set impler=.\java-advanced-2020\modules\info.kgeorgiy.java.advanced.implementor\info\kgeorgiy\java\advanced\implementor\Impler.java
set jar=.\java-advanced-2020\modules\info.kgeorgiy.java.advanced.implementor\info\kgeorgiy\java\advanced\implementor\JarImpler.java
set exception=.\java-advanced-2020\modules\info.kgeorgiy.java.advanced.implementor\info\kgeorgiy\java\advanced\implementor\ImplerException.java


set links=https://docs.oracle.com/en/java/javase/11/docs/api

javadoc %code% -d %out% -private -link %links% %impler% %jar% %exception%
