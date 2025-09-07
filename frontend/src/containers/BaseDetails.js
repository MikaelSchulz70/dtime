import React from "react";
import { useToast } from '../components/Toast';

export default class BaseDetails extends React.Component {
    constructor(props) {
        super(props);
        this.handleError = this.handleError.bind(this);
        this.clearError = this.clearError.bind(this);
        this.clearErrors = this.clearErrors.bind(this);
        this.fieldErrors = new Map(); // Track field errors in state instead of DOM
    }

    clearError(fieldName) {
        // Remove from our field errors map
        this.fieldErrors.delete(fieldName);
        
        // Clear error display using React refs instead of jQuery
        const errorElement = document.getElementById(fieldName + "ErrorMsg");
        if (errorElement) {
            errorElement.textContent = " ";
        }
    }

    clearErrors() {
        // Clear all field errors
        this.fieldErrors.clear();
        
        // Clear all error displays using native DOM instead of jQuery
        const errorElements = document.querySelectorAll('small[id$="ErrorMsg"]');
        errorElements.forEach(element => {
            element.textContent = "";
        });
    }

    handleError(status, error, fieldErrors) {
        if (status === 400 && fieldErrors != null) {
            for (var i = 0; i < fieldErrors.length; i++) {
                var errorItem = fieldErrors[i];
                var fieldName = errorItem['fieldName'];
                var errorMessage = errorItem['fieldError'];
                
                // Store in our map
                this.fieldErrors.set(fieldName, errorMessage);
                
                // Display error using native DOM instead of jQuery
                const errorElement = document.getElementById(fieldName + "ErrorMsg");
                if (errorElement) {
                    errorElement.textContent = errorMessage;
                }
            }
        } else if (status === 500) {
            this.props.showError?.("Internal server error: " + error) || alert("Internal server error:\n" + error);
        } else {
            this.props.showError?.("Error: " + error) || alert("Error:\n" + error);
        }
    }
} 