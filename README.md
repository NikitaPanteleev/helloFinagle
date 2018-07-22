# Running
## How to run
```
sbt run
```
## How to test
```
curl -D - 127.0.0.1:8080/addressBook/1
```

# Yakatak Backend Engineer Test #
The purpose of this test is to do the full cycle of creating a Finagle Thrift
service that depends on other Thrift services. You should create a github repository
and upload the solution. At the end of the test I will expect to receive an email with
the link to the repository.

The code must include unit tests.

This package contains a startup script (bin/test-server), a lib directory
with all the jar files and three thrift files.

The test server exposes two thrift services:

- AddressBookDbService: A service that exposes an interface to the Address Book Database. (There are 10 users in the DB, with IDs 1 to 10)
- UserManagerService: A service that exposes a function to fetch the Yakatak User ID for each Extenal User ID

To see the command line arguments for the test server run bin/test-server -help.

The objective is to write an AddressBookService that combines the information you get
from both backend services.

These are the specifications:

- The addressbook service should have a method that allows the client to traverse the address book.
- The maximum amount of contacts the client can fetch each time is 100.
- The client can ask for: any contacts, contacts with yakatak ID or contacts without yakatak ID.
- The client needs to know after fetching the list of contacts if there are more left to traverse and how to fetch them
- The contact information must contain the Yakatak User ID if present.

Tasks:

- Create a thrift file that follows the specification
- Write the finagle service that implements the service

If you have some extra time you can look at the metrics library provided by Finagle and add some metrics to the service. Easiest way to expose those metrics 
is via the admin interface you get using Twitter Server

Can you tell me if there is any possible optimisation we could do in the backend services to increase performance?

