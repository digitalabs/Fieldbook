'use strict';

/**
 * As bmsAuth is needed from non-angular app like Nursery Manager
 * we create this as global
 *
 * TODO When migrating to angular, transform this to a service
 */
var bmsAuth = (function(bmsAuth, window) {

	// TODO see Workbench/src/main/web/src/apps/ontology/app-services/bmsAuth.js
	bmsAuth.handleReAuthentication = function() {
		alert('Breeding Management System needs to authenticate you again. Redirecting to login page.');
		window.top.location.href = '/ibpworkbench/logout';
	};

	bmsAuth.isValidToken = function() {
		var token = JSON.parse(localStorage.getItem("bms.xAuthToken"));
		return token && token.expires > new Date().getTime();
	};

	return bmsAuth;

})(bmsAuth || {}, window);
