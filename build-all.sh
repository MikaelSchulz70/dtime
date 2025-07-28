#!/bin/bash

echo "Building entire DTime application..."

# Build frontend first
./build-frontend.sh

# Then build backend (which now includes frontend assets)
./build-backend.sh

echo "Complete build finished!"