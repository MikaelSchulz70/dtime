# DTime Database Management Makefile

# Default environment file
ENV_FILE ?= ../.env.local

# Docker compose files - using local docker-compose.yml
COMPOSE_FILE = docker-compose.yml
FULL_COMPOSE = ../docker-compose.yml

# Include environment variables if file exists
ifneq (,$(wildcard $(ENV_FILE)))
    include $(ENV_FILE)
    export
endif

.PHONY: help build start stop restart logs shell backup restore clean health

help: ## Show this help message
	@echo "DTime Database Management Commands:"
	@echo ""
	@grep -E '^[a-zA-Z_-]+:.*?## .*$$' $(MAKEFILE_LIST) | sort | awk 'BEGIN {FS = ":.*?## "}; {printf "\033[36m%-20s\033[0m %s\n", $$1, $$2}'

build: ## Build the PostgreSQL Docker image
	@echo "Building PostgreSQL Docker image..."
	docker-compose -f $(COMPOSE_FILE) build

start: ## Start the PostgreSQL database
	@echo "Starting PostgreSQL database..."
	docker-compose -f $(COMPOSE_FILE) up -d
	@echo "Waiting for database to be ready..."
	@make health

stop: ## Stop the PostgreSQL database
	@echo "Stopping PostgreSQL database..."
	docker-compose -f $(COMPOSE_FILE) down

restart: ## Restart the PostgreSQL database
	@echo "Restarting PostgreSQL database..."
	@make stop
	@make start

logs: ## Show database logs
	docker-compose -f $(COMPOSE_FILE) logs -f postgres

shell: ## Connect to PostgreSQL shell
	docker-compose -f $(COMPOSE_FILE) exec postgres psql -U dtime -d dtime

admin-shell: ## Connect to PostgreSQL as admin
	docker-compose -f $(COMPOSE_FILE) exec postgres psql -U postgres

health: ## Check database health
	@echo "Checking database health..."
	@for i in $$(seq 1 30); do \
		if docker-compose -f $(COMPOSE_FILE) exec -T postgres pg_isready -U dtime -d dtime >/dev/null 2>&1; then \
			echo "✅ Database is ready!"; \
			exit 0; \
		fi; \
		echo "⏳ Waiting for database... ($$i/30)"; \
		sleep 2; \
	done; \
	echo "❌ Database failed to start"; \
	exit 1

backup: ## Create database backup
	@echo "Creating database backup..."
	@mkdir -p backups
	@BACKUP_FILE="backups/dtime_backup_$$(date +%Y%m%d_%H%M%S).sql"; \
	docker-compose -f $(COMPOSE_FILE) exec -T postgres pg_dump -U dtime -d dtime > "$$BACKUP_FILE"; \
	echo "✅ Backup created: $$BACKUP_FILE"

restore: ## Restore database from backup (requires BACKUP_FILE variable)
	@if [ -z "$(BACKUP_FILE)" ]; then \
		echo "❌ Please specify BACKUP_FILE=path/to/backup.sql"; \
		exit 1; \
	fi
	@echo "Restoring database from $(BACKUP_FILE)..."
	@docker-compose -f $(COMPOSE_FILE) exec -T postgres psql -U dtime -d dtime < "$(BACKUP_FILE)"
	@echo "✅ Database restored from $(BACKUP_FILE)"

reset: ## Reset database (WARNING: This will delete all data!)
	@echo "⚠️  WARNING: This will delete all database data!"
	@read -p "Are you sure? (yes/no): " confirm; \
	if [ "$$confirm" = "yes" ]; then \
		echo "Resetting database..."; \
		make stop; \
		docker volume rm database_postgres_data 2>/dev/null || true; \
		make start; \
		echo "✅ Database reset complete"; \
	else \
		echo "❌ Reset cancelled"; \
	fi

clean: ## Clean up database containers and volumes
	@echo "Cleaning up database resources..."
	docker-compose -f $(COMPOSE_FILE) down -v
	docker volume prune -f
	@echo "✅ Cleanup complete"

full-stack: ## Start full application stack (database + backend + frontend)
	@echo "Starting full application stack..."
	docker-compose -f $(FULL_COMPOSE) --profile full-stack up -d

status: ## Show container status
	docker-compose -f $(COMPOSE_FILE) ps

# Development helpers
dev-data: ## Load development data
	@echo "Loading development data..."
	@docker-compose -f $(COMPOSE_FILE) exec -T postgres psql -U dtime -d dtime -f /docker-entrypoint-initdb.d/02-create-dev-data.sql
	@echo "✅ Development data loaded"

monitor: ## Monitor database performance
	docker-compose -f $(COMPOSE_FILE) exec postgres psql -U dtime -d dtime -c "\
		SELECT \
			pid, \
			now() - pg_stat_activity.query_start AS duration, \
			query \
		FROM pg_stat_activity \
		WHERE (now() - pg_stat_activity.query_start) > interval '5 minutes';"