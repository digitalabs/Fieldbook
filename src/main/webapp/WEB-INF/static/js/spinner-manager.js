/* global Spinner */
window.SpinnerManager = (function() {
    'use strict';
    var activeConnectionsAvailable = false;
    var DELAY = 500;

    function startSpinnerIfNecessary() {
        if (activeConnectionsAvailable) {
            Spinner.play();
        }
    }


    return {
        addActive : function() {
            if (!activeConnectionsAvailable) {
                setTimeout(startSpinnerIfNecessary, DELAY);
                activeConnectionsAvailable = true;
            }
        },

        resolveActive : function() {
            activeConnectionsAvailable = false;

            Spinner.stop();
        }
    };
}());