import React from "react";
import $ from 'jquery';

export default class BaseDetails extends React.Component {
    constructor(props) {
        super(props);
        this.handleError = this.handleError.bind(this);
        this.clearError = this.clearError.bind(this);
        this.clearErrors = this.clearErrors.bind(this);
    }

    clearError(fieldName) {
        var divId = "#" + fieldName + "ErrorMsg";
        $(divId).text(" ");
    }

    clearErrors() {
        $('small').text("");
    }

    handleError(status, error, fieldErrors) {
        if (status === 400 && fieldErrors != null) {
            for (var i = 0; i < fieldErrors.length; i++) {
                var errorItem = fieldErrors[i];
                var divId = "#" + errorItem['fieldName'] + "ErrorMsg";
                $(divId).text(errorItem['fieldError']);
            }
        } else if (status === 500) {
            alert("Internal server error:\n" + error);
        } else {
            alert("Error:\n" + error);
        }
    }
} 