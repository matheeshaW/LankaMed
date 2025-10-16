# Database Persistence Fix

## Problem
The database was automatically resetting and repopulating user data every time the backend restarted, causing data loss.

## Root Causes Identified

### 1. JPA Configuration Issue
**File**: `backend/src/main/resources/application.properties`
**Problem**: Line 14 had `spring.jpa.hibernate.ddl-auto=create-drop`
**Impact**: This setting drops and recreates all database tables on every application startup, deleting all data.

### 2. Data Initializer Issue
**File**: `backend/src/main/java/com/lankamed/health/backend/config/DataInitializer.java`
**Problem**: The `DataInitializer` class was running on every startup and recreating all sample data.
**Impact**: Even if tables weren't dropped, all data was being recreated, overwriting existing data.

## Fixes Applied

### Fix 1: Updated JPA Configuration
**Changed**:
```properties
# Before
spring.jpa.hibernate.ddl-auto=create-drop

# After
spring.jpa.hibernate.ddl-auto=update
```

**Explanation**: 
- `create-drop`: Drops and recreates tables on every startup (deletes all data)
- `update`: Only creates tables if they don't exist, updates schema if needed (preserves data)

### Fix 2: Smart Data Initialization
**Added**:
1. **Configuration Property**: `app.data.initialize=true` in application.properties
2. **Empty Database Check**: Only creates sample data if database is completely empty
3. **Configuration Control**: Can disable data initialization by setting `app.data.initialize=false`

**Logic**:
```java
// Only create sample data if database is empty (first time setup)
if (userCount == 0 && doctorCount == 0) {
    System.out.println("DataInitializer: Database is empty, creating initial sample data...");
    createSampleData();
} else {
    System.out.println("DataInitializer: Database already contains data, skipping initialization.");
}
```

## Benefits

### ✅ Data Persistence
- User registrations are preserved across restarts
- Appointments and medical records remain intact
- No more data loss when restarting the backend

### ✅ Smart Initialization
- Sample data only created on first run (empty database)
- Existing data is never overwritten
- Can be disabled completely for production

### ✅ Flexible Configuration
- Easy to enable/disable data initialization
- Clear logging shows what's happening
- Safe for both development and production

## Testing the Fix

### 1. First Startup (Empty Database)
```
DataInitializer: Starting initialization check...
DataInitializer: Data initialization enabled: true
DataInitializer: Current user count: 0
DataInitializer: Current doctor count: 0
DataInitializer: Database is empty, creating initial sample data...
DataInitializer: Created 8 doctors, 4 patients, 3 hospitals, 5 categories, and sample appointments
```

### 2. Subsequent Startups (With Data)
```
DataInitializer: Starting initialization check...
DataInitializer: Data initialization enabled: true
DataInitializer: Current user count: 12
DataInitializer: Current doctor count: 8
DataInitializer: Database already contains data, skipping initialization.
DataInitializer: Existing users: 12, doctors: 8
```

### 3. Disabled Initialization
Set `app.data.initialize=false` in application.properties:
```
DataInitializer: Starting initialization check...
DataInitializer: Data initialization enabled: false
DataInitializer: Data initialization is disabled, skipping...
```

## Configuration Options

### For Development
```properties
# Enable data initialization for development
app.data.initialize=true
spring.jpa.hibernate.ddl-auto=update
```

### For Production
```properties
# Disable data initialization for production
app.data.initialize=false
spring.jpa.hibernate.ddl-auto=validate
```

### For Fresh Start (Reset Database)
```properties
# Reset database (use with caution)
app.data.initialize=true
spring.jpa.hibernate.ddl-auto=create-drop
```

## Verification Steps

1. **Start Backend**: Check console logs for initialization messages
2. **Register User**: Create a new user through the frontend
3. **Restart Backend**: Verify user still exists after restart
4. **Check Logs**: Confirm "skipping initialization" message appears
5. **Verify Data**: All existing data should be preserved

## Files Modified

1. `backend/src/main/resources/application.properties`
   - Changed `ddl-auto` from `create-drop` to `update`
   - Added `app.data.initialize=true` configuration

2. `backend/src/main/java/com/lankamed/health/backend/config/DataInitializer.java`
   - Added configuration property injection
   - Added empty database check
   - Added conditional data creation logic
   - Improved logging and error handling

## Result
✅ **Database persistence is now working correctly!**
- No more data loss on backend restarts
- User registrations are preserved
- Appointments and medical records remain intact
- Smart initialization only runs when needed

