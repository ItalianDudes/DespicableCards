@echo off

echo Cleaning before Deployment
call ./gradlew.bat clean

echo Deploying Linux Jar
call ./gradlew.bat deployJarForLinux

echo Deploying Windows Jar
call ./gradlew.bat deployJarForWindows

echo Deploying Mac Jar
call ./gradlew.bat deployJarForMac

echo Deploying Mac AArch64 (ARM) Jar
call ./gradlew.bat deployJarForMacAArch64

echo Deploying Complete!