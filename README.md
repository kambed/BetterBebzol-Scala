# BetterBebzol

## To start application:

## 1.) Start MySQL database in docker

To start database on docker use command:
> docker-compose -f docker-compose.yml

Alternatively, for JetBrains Intellij users they can use green button on the left in opened docker-compose file in db section.

## 2.) Start Akka backend

To start Akka backend locally in your environment:
> sbt run

Alternatively, for JetBrains Intellij users, that does not have sbt installed locally, click green button in opened Main file

## After starting application:

> Application will be available on [localhost:8080](http://localhost:8080)
> 
> To check api endpoints you can use [Swagger documentation](http://localhost:8080/swagger-ui.html)