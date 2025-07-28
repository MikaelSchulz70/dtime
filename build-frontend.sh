#!/bin/bash

echo "Building React frontend..."
cd frontend
npm install
npm run build
echo "Frontend build completed!"

echo "Copying frontend build to backend static resources..."
mkdir -p ../backend/src/main/resources/static/internal/js
cp -r dist/* ../backend/src/main/resources/static/internal/js/
echo "Frontend assets copied to backend!"