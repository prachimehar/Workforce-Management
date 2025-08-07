# Workforce Management API 🛠️

Welcome! This repository contains my submission for the **Backend Engineer Challenge** at Railse. The challenge involves building and extending a simplified **Workforce Management** API for a logistics super-app.

## 📌 Challenge Overview

This API allows managers to **create, assign, and track tasks** for employees like salespeople or operations staff. The system manages the following entities:

- **Task**: Work unit assigned to staff with a title, status, start date, and due date.
- **Staff**: Employees who can be assigned tasks.
- **Status**: Can be `ACTIVE`, `COMPLETED`, or `CANCELLED`.
- **Priority**: A new feature added to help teams focus on high-importance tasks.
- **Comments & Activity History**: For tracking task updates and discussions.
  
## 🚀 Features Implemented

### ✅ Part 0: Project Setup & Structuring
- Set up a new **Spring Boot** project using **Gradle**.
- Organized code into a clean modular structure:

### 🐞 Part 1: Bug Fixes

#### 🔧 Bug 1: Task Re-assignment Creates Duplicates
- Fixed logic to prevent duplicate task assignments when a task is reassigned.
- Added checks to ensure a staff member can only have one instance of a given task.

#### 🔧 Bug 2: Cancelled Tasks Clutter the View
- Updated task listing logic to **exclude cancelled tasks** from the default view.
- Introduced optional query parameter to include/exclude cancelled tasks.
  
### ✨ Part 2: New Features

#### 🚀 Feature 1: "Smart" Daily Task View
- Implemented an endpoint to fetch tasks due **today**, **prioritized** by:
- Task priority (`HIGH` → `MEDIUM` → `LOW`)
- Start and due time
- Helps staff focus on what needs immediate attention.

#### 🚀 Feature 2: Task Priority
- Added `Priority` field to task model: `LOW`, `MEDIUM`, `HIGH`.
- Included priority handling in creation, editing, and filtering of tasks.
- Prioritized sorting in daily view and reporting endpoints.

#### 🚀 Feature 3: Task Comments & Activity History
- Introduced a **comments** feature on tasks:
- Any update to a task adds an entry to its **activity log**.
- Users can add time-stamped comments (e.g., progress notes, clarification).
- Provides an audit trail for collaboration and transparency.

## 📦 Tech Stack

- **Java 17**
- **Spring Boot**
- **Gradle**
- **Lombok**
- **Postman** (for API testing)
