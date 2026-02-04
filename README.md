# ApplyWise

A Job Application Management Mobile Application

## Overview

ApplyWise is a mobile application designed to help job seekers manage and track their job and internship applications in a structured and organized way. The app provides a centralized system that allows users to clearly monitor their job search process, reducing confusion and missed opportunities.

## Objective

Students and fresh graduates often apply to multiple positions at the same time and struggle to remember application status, interview progress, and response outcomes. ApplyWise aims to solve this problem by providing a reliable tool to organize application records without depending on spreadsheets or manual notes.

## Target Users

- University students actively applying for jobs and internships
- Fresh graduates in the job search process
- Internship seekers managing multiple applications
- Anyone who applies for positions through job portals, company websites, and email referrals

## Features

### Authentication
- User registration with email/password
- Google Sign-In integration
- Profile management with avatar customization

### Application Management
- Add job applications with company name, position, application date, and status
- Update application status following a recruitment workflow:
  - Applied
  - Interview Scheduled
  - Interview Completed
  - Offer Received
  - Rejected
  - No Response
- View application timeline with status history
- Delete applications when no longer needed

### Dashboard
- Overview of all applications with statistics
- Pie chart visualization of application distribution by status
- Quick stats for interviews, offers, and rejections
- Recent applications list

### Application List
- Search applications by company name or job title
- Filter applications by status
- Sort applications by date or alphabetically
- View all application details

### Interview Reminders
- Schedule interview reminders (30 minutes before interview time)
- Notification system for upcoming interviews
- One-tap access to application details from notifications

### Additional Features
- Input validation with error messages
- Offline support with cloud synchronization
- Network status indicator
- Timeline view for tracking application progress
- Notes section for each application

## Technology Stack

### Frontend
- **Kotlin** - Programming language
- **Jetpack Compose** - UI framework
- **Material 3** - Design system
- **Navigation Component** - Screen navigation

### Backend & Services
- **Firebase Authentication** - User account management
- **Firebase Firestore** - Cloud database
- **Firebase Cloud Messaging** - Notifications

### Architecture & Libraries
- **MVVM Architecture** - Clean separation of concerns
- **Hilt/Dagger** - Dependency injection
- **Kotlin Coroutines & Flow** - Async operations
- **WorkManager** - Background tasks
- **AlarmManager** - Scheduled notifications

## Project Structure

```
app/
├── src/main/java/com/team/applywise/
│   ├── core/
│   │   ├── constants/          # App constants
│   │   └── utils/              # Utility functions
│   ├── data/
│   │   ├── model/              # Data models
│   │   └── repo/               # Repository implementations
│   ├── service/                # Authentication, notifications
│   └── ui/
│       ├── components/         # Reusable UI components
│       ├── navigation/         # Navigation setup
│       ├── screens/            # App screens
│       │   ├── application/    # Application management
│       │   ├── dashboard/      # Dashboard
│       │   ├── login/          # Authentication
│       │   ├── profile/        # User profile
│       │   ├── register/       # Registration
│       │   └── splash/         # Splash screen
│       └── theme/              # App theming
```

## Installation

### Prerequisites
- Android Studio Hedgehog or later
- JDK 17 or later
- Android SDK (API 24+)
- Firebase project setup

### Setup Steps

1. Clone the repository
```bash
git clone https://github.com/yourusername/applywise.git
cd applywise
```

2. Open the project in Android Studio

3. Set up Firebase:
   - Create a Firebase project at [Firebase Console](https://console.firebase.google.com)
   - Add an Android app to your Firebase project
   - Download `google-services.json` and place it in `app/` directory
   - Enable Firebase Authentication (Email/Password and Google Sign-In)
   - Create a Firestore database

4. Update the Google Client ID:
   - Open `app/src/main/java/com/team/applywise/core/constants/Constants.kt`
   - Replace `GOOGLE_CLIENT_ID` with your Web client ID from Firebase Console

5. Sync the project with Gradle files

6. Run the app on an emulator or physical device

## Firebase Security Rules

The app uses Firestore security rules to ensure data privacy:

```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    function isAuthenticated() {
      return request.auth != null;
    }

    match /users/{userId} {
      allow read, create, update, delete: if isAuthenticated() && request.auth.uid == userId;
    }

    match /applications/{applicationId} {
      allow read: if isAuthenticated() && resource.data.userId == request.auth.uid;
      allow create: if isAuthenticated() && request.resource.data.userId == request.auth.uid;
      allow update: if isAuthenticated() && resource.data.userId == request.auth.uid;
      allow delete: if isAuthenticated() && resource.data.userId == request.auth.uid;
    }
  }
}
```

## Usage

1. **Registration/Login**: Create an account or sign in with Google
2. **Add Applications**: Tap the + button to add job applications
3. **Track Progress**: Update application status as you progress
4. **View Dashboard**: Monitor your job search statistics
5. **Set Reminders**: Schedule interviews to receive notifications
6. **Search & Filter**: Find specific applications quickly

## Implementation Plan

### Stage 1: Foundation
- Application design and navigation structure
- Database schema design
- UI/UX wireframes

### Stage 2: Core Features
- User authentication implementation
- Job application CRUD operations
- Cloud data synchronization
- Dashboard with statistics

### Stage 3: Enhancement & Testing
- User experience improvements
- Error handling and validation
- Offline support
- Testing and bug fixes

## Known Limitations

- Job application information is entered manually by users
- No automatic integration with external job platforms
- Requires internet connection for initial data sync

## Challenges Addressed

- **Offline Connectivity**: Implemented Firestore offline support and network status monitoring
- **Data Synchronization**: Automatic sync when connection is restored
- **User Feedback**: Clear error messages and loading states

## Future Enhancements

Potential improvements for future versions:
- Integration with public company information APIs
- Follow-up reminder features
- Calendar export options
- Resume/CV attachment support
- Application statistics and insights
- Export data to PDF or spreadsheet

## Contributing

This is an academic project. Contributions are welcome for educational purposes.

## License

This project is developed as part of an academic assignment.

## Contact

For questions or feedback, please open an issue in the repository.

---

**Note**: This application is designed for educational purposes and as a portfolio project demonstrating mobile development skills with Kotlin, Jetpack Compose, and Firebase integration.
