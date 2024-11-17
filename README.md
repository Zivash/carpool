# Carpool App 

## Overview

The Carpool App is designed to revolutionize the way people commute by
offering a smart, efficient, and eco-friendly solution for shared rides. Whether
you're commuting to work, running errands, or heading out for a night on
the town, our app makes it easy to connect with others going your way.

## Table of Contents

1. [Features](#features)
   - [User Registration & Authentication](#user-registration--authentication)
   - [Profile Picture](#profile-picture)
   - [Database Integration](#database-integration)
   - [Image Storage](#image-storage)
   - [Room Database Integration](#room-database-integration)
   - [Ride Details](#ride-details)
2. [Getting Started](#getting-started)
   - [Prerequisites](#prerequisites)
   - [Setup](#setup)
3. [Permissions](#permissions)
4. [Features Breakdown](#features-breakdown)
   - [SignUpFragment](#signupfragment)
   - [SignUpViewModel](#signupviewmodel)
   - [FindRideFragment](#findridefragment)
   - [LoginFragment](#loginfragment)
   - [MyRidesModeFragment](#myridesmodefragment)
   - [DriverInfo Fragment](#driverinfo-fragment)
   - [EmptyRidesFragment](#emptyridesfragment)
5. [Carpool App - Local Database Setup](#carpool-app---local-database-setup)
6. [License](#license)
7. [Acknowledgments](#acknowledgments)

---

## Features

### User Registration & Authentication
- Users can sign up and log in using email and password with Firebase Authentication.

### Profile Picture
- Users can upload or take a new picture for their profile.

### Database Integration
- User data (name, email, phone number, profile picture) is stored in Firebase Realtime Database.

### Image Storage
- User profile pictures are stored in Firebase Storage.

### Room Database Integration
- Uses Room for local storage of user details (ID, email, password) and allows offline data persistence.

### Ride Details
- Displays information about carpool rides, including the origin, destination, available seats, price, and driver details.

---

## Getting Started

### Prerequisites

Before you begin, ensure you have the following installed:

- **Android Studio**: The official IDE for Android development.
- **Firebase Project**: Set up a Firebase project and integrate Firebase Authentication, Firebase Realtime Database, and Firebase Storage with your Android project.

### Setup

1. Clone the repository:
   ```bash
   git clone https://github.com/your-username/carpool-app.git


## Features
- **Room Database**: Stores user details like ID, email, and password.
- **User Repository**: Manages operations like adding a user and retrieving user details.
- **Ride Model**: Contains information about a ride, including origin, destination, available seats, price, and driver details.
- **Location Autocomplete**: Users can select the origin and destination locations using Google Places Autocomplete.
- **Current Location**: Users can use their current GPS location for either the origin or destination.
- **Date Picker**: A date picker is used to select the ride date.
- **User Authentication**: 
