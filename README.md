# LaundryDemoServer
This repo is a snapshot of an application written for a friend's laundromat.  The goal was to build out a mobile app for them.  Development continued from here in a private repo, with this repo kept as a portfolio piece, and a place for me to play with different patterns on server architecture.

# Architecture

This server uses Spring Boot, Postgres and Hibernate for its  database access, with Flyway managing its migrations.

## Models

The DB entities are in model.dbview.  These are dumb models, and all derive from BaseEntity which defines their UUID primary key.  When these entities need different variations (such as for being sent to customers or uploaded by customers), these variations are in different model packages.  For now, we have customerview, which holds upload and download version for customers.  These may drop fields the customer doesn't need (such as organization ids), or add fields from other tables (such as item names being taken from translation tables and added to the item).

Every item that is uploaded from a user has a validator in models.validation.  These validators inspect the object and accumulate errors in a list.  There are also validators for specific data fields like passwords and phone numbers.  Validators may (and should) call each other if needed.

## Controllers

Controllers are all places in the controllers package.  Each controller  is in charge of 1 closely related set of endpoints provided to the user.  They should not have any business logic that's in the Services.  Their responsibility is to take data from the request and send it to the service, and take data from the Service and send it to the user.  It is responsible for any model conversion to specific forms, but not for gathering, filtering, or validating data.

## Services

Services exist in packages relating to their functionality, rather than in a central place.  Each service is responsible for a business logic unit, not for a specific db object.  For example, LoginAuthenticator owns login and authenticating users, so owns the password and sessions tables.  Services are the places that call repositories for data, do validation, and know how to combine data from multiple tables.

## Dealing with bad data

Any service that finds incorrect data should throw an APIErrorException.  A tob level APIExceptionHandler will catch that and return the proper result for all controllers

Any service that finds bad data in the db and cannot continue should throw a DatabaseDataInvalidException.  Heavy alerting should be put on logs to find and fix these.  The known case that throws it is if we find multiple tokens on login, but that should be prevented by the DB constraints so we should never see it.

## Dealing with time

All times are stored in the DB as UTC times, and should be converted for display.  All times send over the network should be UTC times.  

If a Service needs to know the current time, it should take a TimeSource as a parameter and use the now function on it.  This will assure it gets UTC time, as well as allow us to mock out time in tests.

