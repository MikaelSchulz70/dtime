# Frontend Production Dockerfile
FROM nginx:alpine

# Copy pre-built assets
COPY dist /usr/share/nginx/html

# Copy custom nginx configuration and entrypoint script
COPY nginx.conf /etc/nginx/conf.d/default.conf
COPY docker-entrypoint.sh /usr/local/bin/docker-entrypoint.sh

# The nginx user already exists in the base image, so we'll just use it

# Make entrypoint executable and set ownership
RUN chmod +x /usr/local/bin/docker-entrypoint.sh

# Expose port 80
EXPOSE 80

# Use entrypoint script
ENTRYPOINT ["/usr/local/bin/docker-entrypoint.sh"]