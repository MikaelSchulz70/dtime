# DTime Frontend

React-based frontend for the DTime time tracking application with runtime environment configuration and Docker support.

## 🚀 Quick Start

### Development Mode
```bash
npm install
npm start
```
Opens [http://localhost:3000](http://localhost:3000) with hot reload.

### Production Build
```bash
npm run build
```
Builds optimized production bundle in `build/` folder.

### Docker Development
```bash
# From project root
docker-compose --profile full-stack up -d
```

## 🐳 Docker Support

### Development Image
```bash
# Build development image (from project root)
./build-frontend-docker.sh --dev-only

# Run development container
docker run -p 3000:3000 dtime-frontend-dev:latest
```

### Production Image
```bash
# Build production image (from project root)  
./build-frontend-docker.sh --prod-only

# Run production container
docker run -p 3000:80 dtime-frontend:latest
```

## ⚙️ Environment Configuration

### Runtime Environment Variables

The application uses **runtime environment injection** - no secrets are baked into the build:

```bash
# Backend API URL
REACT_APP_BACKEND_URL=http://localhost:8080

# Environment mode
NODE_ENV=production
```

### Development Proxy

The webpack dev server proxies API calls to the backend:
- `/api/*` → Backend at port 8080
- `/perform_login` → Backend authentication
- `/logout` → Backend logout

## 📁 Project Structure

```
frontend/
├── src/
│   ├── components/         # Reusable components
│   │   └── Login.js       # Login component
│   ├── containers/        # Page components
│   │   ├── menu/          # Navigation
│   │   ├── report/        # Reporting pages
│   │   └── timereportstatus/
│   ├── assets/            # Images and static files
│   ├── index.js           # Application entry point
│   └── App.js             # Main app component
├── public/
│   ├── index.html
│   └── config.js          # Runtime config (injected by Docker)
├── Dockerfile             # Production build
├── Dockerfile.dev         # Development build  
├── docker-entrypoint.sh   # Runtime config injection
├── package.json
└── webpack.config.js      # Development server config
```

## 🔧 Available Scripts

### `npm start`
Development server with hot reload at [http://localhost:3000](http://localhost:3000).

### `npm test`
Runs tests in watch mode.

### `npm run build`
Production build with optimized bundle.

### `npm run eject`
**⚠️ One-way operation!** Exposes all configuration files.

## 🎨 Technology Stack

- **React 18.3.1** - UI library
- **Webpack 5.97.1** - Module bundler  
- **Bootstrap 5.3.8** - CSS framework
- **Axios** - HTTP client
- **React Router 5.3.4** - Routing
- **Nginx** - Production web server

## 🔐 Authentication

The frontend handles login UI while delegating authentication to the Spring Boot backend:

1. User submits credentials via React login form
2. Form posts to `/perform_login` endpoint
3. Backend validates and creates session
4. Frontend redirects on successful authentication

## 🚀 Deployment

### Docker Production Deployment
```bash
# From project root
./build-docker.sh --frontend-only
./deploy.sh --env production
```

### Manual Production Deployment
```bash
npm run build
# Serve build/ folder with web server
```

## 🛠️ Development

### Prerequisites
- Node.js 16+
- npm or yarn

### Setup
```bash
npm install
```

### Environment Setup
Backend must be running on port 8080 for API calls to work in development mode.

## 📚 Learn More

- [Create React App Documentation](https://facebook.github.io/create-react-app/docs/getting-started)
- [React Documentation](https://reactjs.org/)
- [Bootstrap Documentation](https://getbootstrap.com/docs/5.3/)
