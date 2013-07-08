#!/bin/bash
cd ~/git_repos/love-bot
export JAR_PATH=~/.m2/repository/bot/LoveBot/0.1-SNAPSHOT/LoveBot-0.1-SNAPSHOT.jar
export LOG_FILE=/home/pi/logs/bot_logs/$1`date +"%Y-%m-%d-%H-%M"`.log


java -cp $JAR_PATH com.bot.main.MainBotResponder $1 >> $LOG_FILE 2>&1

