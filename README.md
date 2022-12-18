# Protech Doctolib


## Description
### Goal
The goal of this project is to create the backend of an appointment scheduler. The frontend linked to the appointment scheduler can be found on [this gitlab repo](https://gitlab.emse.fr/protech-doctolib/protech_doctolib_frontend).

### How to use it?
To configure this application go to src>main>resources>application.properties.
On the bottom of the file you can configure the backend and the frontend url.
By default, the application is locally launched at http://localhost:8080.

This project is a Spring Boot application using gradle. To launch it, go on the root folder and use:
```
./gradlew bootRun
```
Then, on your web browser, go to the backend url you previously specified. You will be prompted to enter a login and a password. By default, the user `admin@gmail.com` with the password `admin` is created with admin rights. You can also use the user `user@gmail.com` with the password `user` to have the user rights. You can then test the API with swagger by going to {backend_url_you_specified}/swagger-ui/.

### What is it doing
This project is the backend of an appointment scheduler. It deploys a REST API (by default on `localhost/8080`). It contains:
- Spring security with a registration API that asks for confirmation by email when a new account is created.
- A time-slot manager API that allows to **create**/**modify**/**delete**/**find by id**/**find all** time-slots in which clients can make appointments.
- An appointment manager API that allows to **create**/**modify**/**delete**/**find by id**/**find all**/**find all by client id** appointments made.
- A user manager API that allows to **modify**/**delete**/**find by id**/**find all** clients and a controllers to handle forgot passwords.
- A registration manager API that allows to create a user and activate the account when the confirmation link is clicked.
- An email sender to send confirmations and reminders emails to users and information emails to admins.

This project has different access rights that allow users to access only some API. Admins have access to all apis but users have some restrictions. For instance, users are unable to access data of other users.

> :warning: **Regarding create and modification apis**: The default json proposed by swagger is not correct. Please use the formatting returned by the findById method. 

### Use
This repo is used by students of Mines Saint-Étienne Engineering school.

## Contributing
This project is not open to contributions.

## Licence
This project is licenced under MIT license.

## Contact
You can contact us by email at the address `quentin.rey@etu.emse.fr` 
