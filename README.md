# 🏠 KaamKaaj - Your Home, Your Tasks, All in One Place

**KaamKaaj** is a comprehensive, modern Android application built with Jetpack libraries and Material Design 3, designed to manage household chores, bills, expenses, family tasks, maintenance schedules, product warranties, and reminders seamlessly in one unified app.

---

## ✨ Key Features & Modules

### 1. 📊 Home Dashboard
- **Overview Cards**: Real-time stats for Total Bills, Monthly Expenses, Next Upcoming Bill, Maintenance Status, Pending Tasks, and Active Reminders.
- **Quick Access**: Fast navigation across all household management modules.
- **Dynamic Status**: Intelligent status updates ("Looking fine", "Check soon", "Needs attention") based on pending dues.

### 2. 🧹 Family Members & Task Management (Family Chores)
- **Family Members**: Add and manage family members (Admin, Father, Mother, Siblings, Self) with customized avatars and roles.
- **Chore Delegation**: Assign household chores (Grocery Shopping, Cleaning, Electricity Bill Payment, Water Can Booking, Medicine Purchase) to specific family members.
- **Status Tracking**: Toggle tasks between **Pending** and **Completed** with point tracking.

### 3. 🛠️ Maintenance & Services
- **Appliance & Vehicle Servicing**: Track maintenance for AC, Refrigerator, Washing Machine, Water Purifier, Bike, Car, Plumbing, and Electrical repairs.
- **Health Indicators**: Good, Warning, and Alert status counters for equipment health.
- **Service Details**: Record Service Name, Product Name, Last Service Date, Next Service Date, Service Cost (₹), and Service Notes with automatic reminder scheduling.

### 4. 🏷️ Warranty & Documents Manager
- **Product Warranty Tracking**: Store warranty details for home products (Samsung TV, LG Refrigerator, Washing Machine, AC).
- **Expiry Countdown**: "Expiring Soon" alert banners and countdowns (e.g., "18 days left").
- **Document Info**: Record purchase dates, warranty duration, brand, and receipt/bill details.

### 5. 💰 Expense Tracking & Budgeting
- **Categories**: Grocery, Electricity, Water, Rent, Internet, Maintenance, Shopping, and Other.
- **Monthly Navigation**: View expenses for any month (January, February, etc.).
- **Visual Analytics**: Interactive **Donut Chart** with category legend, amounts, and spending percentages.

### 6. ⏰ Reminders & Notifications
- **AlarmManager Integration**: High-priority system notifications for bills (1 day before & due day), maintenance, warranties, and custom reminders.
- **Custom Time Reminders**: Set exact date and time reminders.
- **Instant Test Feature**: Built-in test notification trigger in settings.

### 7. 👤 Profile & Settings (Dark / Light Mode)
- **Dynamic Theming**: Seamless switching between Light Mode (White/Clean background) and Dark Mode.
- **Account Management**: Secure local authentication (Sign up, Login, Password change), Edit Profile, and Data management.

---

## 🛠️ Tech Stack & Architecture
- **Language**: Java & Kotlin
- **UI Toolkit**: Material Design 3 (M3), ConstraintLayout, CoordinatorLayout, RecyclerView, DonutView (Custom Canvas)
- **Local Storage**: SharedPreferences with structured JSON serialization
- **Notifications**: AlarmManager, BroadcastReceiver (`ReminderReceiver`), NotificationChannel (`IMPORTANCE_HIGH`)
- **Edge-to-Edge**: Modern immersive Android window layout (`androidx.activity.EdgeToEdge`)

---

## 🚀 Getting Started
1. Clone the repository:
   ```bash
   git clone https://github.com/zeelpanchal777444-ops/KaamKaaj.git
   ```
2. Open the project in **Android Studio**.
3. Sync Gradle and run the app on an Android Emulator or physical device (API 23+).

---
*Built with passion for smarter household management.*
