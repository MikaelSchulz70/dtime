// Remove jQuery dependency for header management
export function Headers() {
    const headerContentType = { 'Content-Type': 'application/json' };

    // Try to get CSRF token from cookie first (for dev server)
    const cookieMatch = document.cookie.match(/XSRF-TOKEN=([^;]+)/);
    if (cookieMatch) {
        // Decode the URL-encoded token
        const token = decodeURIComponent(cookieMatch[1]);
        const headers = {
            'Content-Type': 'application/json',
            'X-XSRF-TOKEN': token
        };
        return { headers: headers };
    }

    // Fallback to meta tag (for backend-served pages)
    const meta = document.querySelector("meta[name='_csrf']");
    if (!meta) {
        return { headers: headerContentType };
    }

    const token = meta.getAttribute("content");
    if (!token) {
        return { headers: headerContentType };
    }

    const headers = {
        'Content-Type': 'application/json',
        'X-CSRF-TOKEN': token
    };

    return { headers: headers };
}

// Utility function for handling API errors consistently
export function handleApiError(error, customMessage = null) {
    console.error('API Error:', error);
    
    if (error.response) {
        // Server responded with error status
        const status = error.response.status;
        const data = error.response.data;
        
        if (status === 401) {
            // Unauthorized - reload the page to show login component
            window.location.reload();
            return;
        }
        
        if (status === 403) {
            return 'You do not have permission to perform this action.';
        }
        
        if (status >= 500) {
            return customMessage || 'A server error occurred. Please try again later.';
        }
        
        // Try to extract message from response
        if (data && data.message) {
            return data.message;
        }
        
        if (data && data.error) {
            return data.error;
        }
        
        return customMessage || `Request failed with status ${status}`;
    }
    
    if (error.request) {
        // Network error
        return 'Network error. Please check your connection and try again.';
    }
    
    // Something else happened
    return customMessage || error.message || 'An unexpected error occurred.';
}

// Utility for making consistent API calls
export function createApiConfig(additionalHeaders = {}) {
    const { headers } = Headers();
    
    return {
        headers: {
            ...headers,
            ...additionalHeaders
        },
        timeout: 30000, // 30 second timeout
    };
}