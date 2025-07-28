# Database Quick Start Guide

## 🚀 Start the Database (2 methods)

### Method 1: Docker Compose (Direct)
```bash
cd database
docker-compose up -d
```

### Method 2: Makefile (Recommended)
```bash
cd database
make start
```

## 📋 Common Commands

```bash
# Check if database is ready
make health

# View logs
make logs

# Connect to database
make shell

# Stop database
make stop

# Create backup
make backup

# See all available commands
make help
```

## 🔗 Connection Details

- **Host**: localhost
- **Port**: 5432  
- **Database**: dtime
- **Username**: dtime
- **Password**: Set in .env file (default: dtime_dev_password)

## 🛠 Setup Environment (Optional)

```bash
# Copy environment template
cp .env.example .env

# Edit with your values
nano .env
```

## 🏥 Health Check

```bash
# Quick health check
make health

# Or manually
docker-compose exec postgres pg_isready -U dtime -d dtime
```

## 🧹 Cleanup

```bash
# Stop and remove containers
make clean

# Reset database (WARNING: deletes all data!)
make reset
```

That's it! Your PostgreSQL database should now be running and ready for the DTime application. 🎉