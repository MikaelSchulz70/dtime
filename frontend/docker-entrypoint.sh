#!/bin/sh

# Create runtime configuration file
cat <<EOF > /usr/share/nginx/html/config.js
window.APP_CONFIG = {
  REACT_APP_BACKEND_URL: '${REACT_APP_BACKEND_URL:-http://localhost:8080}',
  NODE_ENV: '${NODE_ENV:-production}'
};
EOF

echo "Generated runtime config:"
cat /usr/share/nginx/html/config.js

# Start nginx
exec nginx -g "daemon off;"