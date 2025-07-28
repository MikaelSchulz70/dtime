FROM postgres:15-alpine

# Set environment variables
ENV POSTGRES_DB=dtime
ENV POSTGRES_USER=dtime
ENV POSTGRES_PASSWORD=dtime_dev_password

# Install additional extensions if needed
RUN apk add --no-cache \
    postgresql-contrib

# Copy initialization scripts
COPY scripts/ /docker-entrypoint-initdb.d/

# Create directory for custom configurations
RUN mkdir -p /etc/postgresql/conf.d

# Copy custom PostgreSQL configuration
COPY postgresql.conf /etc/postgresql/postgresql.conf

# Set the default command
CMD ["postgres", "-c", "config_file=/etc/postgresql/postgresql.conf"]

# Expose PostgreSQL port
EXPOSE 5432

# Add labels for documentation
LABEL maintainer="DTime Development Team"
LABEL description="PostgreSQL database for DTime application"
LABEL version="15-alpine"