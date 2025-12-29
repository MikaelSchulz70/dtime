import { useRef, useCallback } from "react";
import { useToast } from '../components/Toast';

export function useBaseDetails() {
    const { showError } = useToast();
    const fieldErrors = useRef(new Map()); // Track field errors

    const clearError = useCallback((fieldName) => {
        // Remove from our field errors map
        fieldErrors.current.delete(fieldName);
        
        // Clear error display using React refs instead of jQuery
        const errorElement = document.getElementById(fieldName + "ErrorMsg");
        if (errorElement) {
            errorElement.textContent = " ";
        }
    }, []);

    const clearErrors = useCallback(() => {
        // Clear all field errors
        fieldErrors.current.clear();
        
        // Clear all error displays using native DOM instead of jQuery
        const errorElements = document.querySelectorAll('small[id$="ErrorMsg"]');
        errorElements.forEach(element => {
            element.textContent = "";
        });
    }, []);

    const handleError = useCallback((status, error, fieldErrorsList) => {
        if (status === 400 && fieldErrorsList != null) {
            for (var i = 0; i < fieldErrorsList.length; i++) {
                var errorItem = fieldErrorsList[i];
                var fieldName = errorItem['fieldName'];
                var errorMessage = errorItem['fieldError'];
                
                // Store in our map
                fieldErrors.current.set(fieldName, errorMessage);
                
                // Display error using native DOM instead of jQuery
                const errorElement = document.getElementById(fieldName + "ErrorMsg");
                if (errorElement) {
                    errorElement.textContent = errorMessage;
                }
            }
        } else if (status === 500) {
            showError?.("Internal server error: " + error) || alert("Internal server error:\n" + error);
        } else {
            showError?.("Error: " + error) || alert("Error:\n" + error);
        }
    }, [showError]);
    
    return {
        handleError,
        clearError,
        clearErrors
    };
}

// Legacy export removed - use useBaseDetails hook instead 