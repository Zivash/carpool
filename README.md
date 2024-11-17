# Carpool App 

## Table of Contents

1. [Overview](#overview)
2. [Features](#features)
3. [Permissions](#permissions)
4. [Dependencies](#dependencies)

---

## Overview

The Carpool App is designed to revolutionize the way people commute by
offering a smart, efficient, and eco-friendly solution for shared rides. Whether
you're commuting to work, running errands, or heading out for a night on
the town, our app makes it easy to connect with others going your way.

---

## Features

### User Registration & Authentication
- **Sign-up and Login**: Users can sign up and log in using email and password with Firebase Authentication.
- **User Session Management**: Upon successful login, users are navigated to the main app screen, `FindFragment`, and their email is displayed as the username.

### Profile Picture
- **Profile Image Upload**: Users can upload or take a new picture for their profile, which is stored in Firebase Storage.

### Database Integration
- **Firebase Realtime Database**: User data (name, email, phone number, profile picture) is stored in Firebase Realtime Database.
- **Offline Storage**: Utilizes **Room Database** to locally store user details like ID, email, and password, allowing offline access.

### Ride Details
- **Ride Model**: The app displays ride details, including origin, destination, available seats, price, and driver information.
- **Seats Management**: Users can join or create rides, and available seats are updated in real-time.

### Location-based Ride Searching
- **FindRideFragment**: Users can search for carpool rides based on origin, destination, available seats, and ride date.
- **Location Autocomplete**: Users can select origin and destination locations via Google Places Autocomplete.
- **Current Location**: Users can use their GPS location for either origin or destination.

### Driver Interaction
- **DriverInfo Fragment**: Displays driver details (name, phone number, and picture) and allows users to contact the driver or join a ride.
- **Join Ride**: Users in "Find" mode can join a ride, which updates ride data in Firebase by reducing available seats and adding the user as a passenger.

### Ride Management
- **MyRidesModeFragment**: Allows users to switch between modes (Driver/Passenger), view, or add their rides.
- **EmptyRidesFragment**: Displays a message when no rides are available in the selected mode.

### Data Persistence
- **Room Database**: Stores user details for persistent storage of their login data.

---

## Permissions

The app requires the following permissions:

- **Camera**: To take profile pictures.
- **Storage**: To save images to the gallery.
- **ACCESS_FINE_LOCATION**: To access the user’s location for determining their current position.

---

## Dependencies

- **Google Places API**
- **Google Maps API** (for LatLng functionality)
- **ViewModel** and **LiveData** for managing the state of UI components
