love-bot
========

This is a project I created to play around with [markov chaining](https://en.wikipedia.org/wiki/Markov_chain) as a language simulator. I wanted to see how similar to normal speech the algorithm would produce when applied to an author with a moderate corpus. I have applied it to both H.P. Lovecraft and Edgar Allan Poe, and created the two twitter accounts @HPbotcraft and @EdgarAllanBot to get them to talk to one another.

This codebase compiles with maven and the twitter authentication keys are place in a properties.xml file. 

It can run in two ways. The first responds to each tweet that has been tweeted at it. The second is to send a unique tweet to each of its followers.

Setup
-----

* Download code: 
	git clone https://github.com/tchapm/love-bot
* Place your twitter authentication info in the properties file:

	"...
	<entry key="oauth.consumer.key">your key</entry>
	<entry key="oauth.consumer.secret">your secret</entry>
	<entry key="YourBotName.token">bot name</entry>
	<entry key="YourBotName.secret">bot secret</entry>
	..."

* Place corpus in src/main/resources/YourBotName_text folder
* Compile: mvn -DbotName=YourBotName

Running
-------
* Run response: java -cp ~/.m2/repository/bot/LoveBot/0.1-SNAPSHOT/LoveBot-0.1-SNAPSHOT.jar com.bot.main.MainBotResponder YourBotName TimeYouWantToRefresh
* Run tweets at followers: java -cp ~/.m2/repository/bot/LoveBot/0.1-SNAPSHOT/LoveBot-0.1-SNAPSHOT.jar com.bot.main.MainBotFollowers YourBotName

Contact
-------
tchap00@gmail.com