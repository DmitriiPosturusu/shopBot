### Telegram Bot Functionality Overview

This Telegram bot offers a wide range of features catering to both regular users and administrators, enhancing interaction and management within the platform.

For more information about bot functionality please visit [Bot commands documentation.](https://github.com/DmitriiPosturusu/shopBot/blob/master/documentation/bot-command/bot_command_en.txt)

---

#### **User Features**:
- **User Registration**: Automatically registers new users by checking the database and adding them if they don’t already exist.
- **Menu Navigation**: Users can navigate the menu to browse products, select quantities, and add items to their shopping cart for later purchase.
- **Shopping Cart**: Displays the shopping cart contents, allowing users to view, modify, and manage their selected items.
- **Personal Settings**: Users can set their preferred language, choosing between English or Romanian and set phone number.
- **Support Access**: Provides users with a way to contact support for assistance or inquiries.
- **Message to Self**: Users can send personal messages to themselves for future reference or reminders.

---

#### **Admin Features**:
- **Product Management**: Administrators can view all products, including those hidden from regular users, and control product visibility.
- **Broadcast Messaging**: Admins have the ability to send messages to all users, functioning as a communication tool for announcements or updates.
- **Cloud Integration**: The bot supports image imports from AWS S3, allowing admins to manage product images directly through cloud storage.
- **File Uploads**: Admins can upload CSV files to import product data into the database, streamlining product management.
- **Product-Image Matching**: The bot automatically associates images with products based on file names, ensuring that product images are accurately displayed when users browse items.

---

### Summary:
This bot serves as an efficient tool for both users and admins. Regular users benefit from browsing and selecting products, managing shopping carts, and choosing their preferred language, while admins gain powerful tools for product management, mass communication, and cloud-based imports.