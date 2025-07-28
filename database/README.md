# DTime Database Setup

This directory contains the PostgreSQL database configuration for the DTime application.

## Quick Start

### 1. Database Only (Recommended for Development)

```bash
# From the database directory
cd database
docker-compose up -d

# Or use the Makefile (easier)
make start
```

### 2. Full Stack (All Services)

```bash
# From the project root
docker-compose --profile full-stack up -d

# Or from database directory
cd database
make full-stack
```

## Directory Structure

```
database/
├── Dockerfile              # PostgreSQL Docker image
├── docker-compose.yml      # Docker Compose configuration
├── postgresql.conf         # PostgreSQL configuration
├── scripts/                # Database initialization scripts
│   ├── 01-init-database.sql   # Initial setup and roles
│   └── 02-create-dev-data.sql # Development seed data
├── data/                   # Persistent data (mounted volume)
├── backups/               # Database backups
├── logs/                  # PostgreSQL logs
├── Makefile              # Database management commands
└── README.md             # This file
```

## Database Configuration

- **Database**: `dtime`
- **User**: `dtime`
- **Password**: Set via `DATABASE_PASSWORD` environment variable
- **Port**: `5432`
- **Version**: PostgreSQL 15 Alpine

## Environment Variables

Create a `.env.local` file in the project root:

```bash
# Database
DATABASE_PASSWORD=your_secure_password

# Optional: Override defaults
POSTGRES_DB=dtime
POSTGRES_USER=dtime
```

## Management Commands

Use the Makefile for easy database management:

```bash
cd database

# Basic operations
make start          # Start database
make stop           # Stop database
make restart        # Restart database
make logs          # View logs
make health        # Check if database is ready

# Database access
make shell         # Connect to database as dtime user
make admin-shell   # Connect as postgres admin

# Backup and restore
make backup        # Create timestamped backup
make restore BACKUP_FILE=backups/backup.sql

# Development
make dev-data      # Load development seed data
make reset         # Reset database (deletes all data!)
make clean         # Remove containers and volumes

# Monitoring
make status        # Show container status
make monitor       # Show long-running queries
```

## Connection Details

### From Host Machine
```bash
# Connection string
jdbc:postgresql://localhost:5432/dtime

# psql command
psql -h localhost -p 5432 -U dtime -d dtime
```

### From Spring Boot Application
```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/dtime
    username: dtime
    password: ${DATABASE_PASSWORD}
```

### From Docker Network
```yaml
spring:
  datasource:
    url: jdbc:postgresql://dtime-db:5432/dtime
    username: dtime
    password: ${DATABASE_PASSWORD}
```

## Database Schema

The database schema is managed by **Liquibase** in the Spring Boot application:
- Location: `backend/src/main/resources/db/changelog/`
- Migrations run automatically when the application starts
- Manual schema changes should be avoided

## Initialization Scripts

1. **01-init-database.sql**: Sets up database roles and permissions
2. **02-create-dev-data.sql**: Loads development seed data (commented out by default)

## Backup and Recovery

### Automatic Backups
```bash
# Create backup with timestamp
make backup
# Output: backups/dtime_backup_20240701_143022.sql
```

### Manual Backup
```bash
# From host
docker-compose -f ../docker-compose.db.yml exec dtime-db pg_dump -U dtime -d dtime > backup.sql

# With compression
docker-compose -f ../docker-compose.db.yml exec dtime-db pg_dump -U dtime -d dtime | gzip > backup.sql.gz
```

### Restore
```bash
# Using Makefile
make restore BACKUP_FILE=backups/dtime_backup_20240701_143022.sql

# Manual restore
docker-compose -f ../docker-compose.db.yml exec -T dtime-db psql -U dtime -d dtime < backup.sql
```

## Troubleshooting

### Database Won't Start
```bash
# Check logs
make logs

# Check container status
make status

# Reset if corrupted
make reset
```

### Connection Issues
```bash
# Test connection
make health

# Check if port is available
netstat -an | grep 5432

# Ensure container is running
docker ps | grep postgres
```

### Performance Issues
```bash
# Monitor active queries
make monitor

# Check database size
make shell
SELECT pg_size_pretty(pg_database_size('dtime'));
```

## Production Considerations

### Security
- Use strong passwords in production
- Enable SSL/TLS connections
- Configure proper firewall rules
- Regular security updates

### Performance
- Tune `postgresql.conf` for your workload
- Monitor and analyze slow queries
- Set up connection pooling
- Configure appropriate memory settings

### Backup Strategy
- Automated daily backups
- Test restore procedures regularly
- Consider point-in-time recovery
- Store backups securely off-site

### Monitoring
- Set up database monitoring (e.g., pgAdmin, Grafana)
- Monitor disk space usage
- Track connection counts
- Monitor query performance

## Development Tips

1. **Use the Makefile commands** for consistency
2. **Always backup** before major changes
3. **Test migrations** on development data
4. **Monitor logs** during development
5. **Use health checks** to ensure database readiness