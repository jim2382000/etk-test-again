/* public-resource */
"use strict";

/*
 * Experimental library for getting (and keeping refreshed) entellitrak access tokens.
 *
 * Some functions on this object return "auth"s. Other functions on this object actually make ajax calls
 * (usually using the auths mentioned previously).
 * 
 * The auths will have getAccessTokenPromise functions
 * which, when called will return promises for tokens which if used immediately after, should not be expired.
 * Auths also know the URL of the entellitrak site which authenticated them so that it does not need to be
 * respecified when performing the endpoint requests.
 *
 * Possible Grant Types
 *   - current user - supported
 *   - client_credentials: clientId/secret - supported
 *   - password:
 *       - local: username/password - supported
 *       - sso remote user - unsupported
 *       - sso request header - unsupported
 *
 * The current strategy for refreshing the token is to check when returning the access token promise whether the token
 * is going to expire soon, and if so, refreshing it at that point. One benefit of this approach over continually refreshing the
 * token automatically is that in the common scenario of the tokens only being needed at page load, we will not need to continually
 * ask the server for new tokens. Other approaches are also possible such as attempting to fetch a new token after a call
 * to an endpoint fails due to an expired token. I have decided not to wait until the call fails for two main reasons. The first
 * is that when the page first loads all calls would fail due to an expired token. The second is that core does not provide a
 * good way to determine whether the call failed due to an expired token (We would have to inspect a string which might change in the future).
 *
 * Can also look into caching (as in singletons) configurations, for instance if there are multiple pieces of code which
 * each try to get a new token for the current user we could reuse the tokens from the first call. I can't currently think of a good
 * reason to not do this.
 * Note: I have implemented the caching for the current user auth.
 */
window.EntellitrakOAuth = (function(jQuery) {

    /* Business key of a page which gets the active workspace name */
    var WORKSPACE_PAGE_BUSINESS_KEY = "net.micropact.aea.oAuth.page.getActiveWorkspaceName"

    /* Number of seconds before a token expires to wait before attempting to refresh it */
    var REFRESH_THRESHOLD_SECONDS = 60

    /* URL of core's page which generates new tokens (although not for the current user version) */
    var NEW_TOKEN_URL = "auth/oauth/token"

    /*
     * Make an automatic token refresher.
     * 
     * This is the most important (and complex) function in the library. It contains the generic logic/pattern for 
     * getting an access token and refreshing it when appropriate.
     * This function needs to be told how to get the initial token, and how to refresh the token.
     * This is because for some grant types (like the current user), fetching the initial token is different from subsequent
     * tokens. Other grant_types like client_credentials are simpler because fetching subsequent tokens is the same as fetching
     * the original.
     *
     * url: String
     *      The URL of the entellitrak site. Should NOT contain a trailing slash. May be empty for the current site.
     * initialTokenInfo: Promise<TokenDetails>
     *      Function which fetches the initial token details. It is expected that the access_token within the details is not expired
     * refreshTokenFn: TokenDetails -> Promise<TokenDetails>
     *      Function which takes the previous token details and fetches new token details.
     */
    function makeAutomaticTokenRefresher(url, initialTokenInfo, refreshTokenFn) {

        /*
         * A flag to keep track of whether we're currently in the process of refreshing the token.
         * This is so that if we're asked for token while we're already in the process of refreshing it, we'll
         * be able to use the result of that refresh instead of trying to get multiple new tokens.
         * Is initialized to true because on creation we'll be in the process of getting the initial token.
         */
        var isTheTokenCurrentlyInTheProcessOfBeingRefreshed = true
        /* The point in time we calculate that the token is no longer safe to return. This will be some time before the actual expiration time. */
        var refreshTime
        /*
         * The token details. So far this just needs to hold the responses from calling token endpoints.
         * The following properties are expected:
         *  - access_token: This is what the getAccessToken function returns
         *  - expires_in: The number of seconds until the token expires. This is necessary to know when to refresh the token.
         * Other properties may be necessary for the refreshTokenFn such as refresh_token, but are not needed by makeAutomaticTokenRefresher.
         */
        var tokenDetailsPromise = initialTokenInfo
            .done(handleIncomingTokenDetails)

        /*
         * Get a promise for the access_token (note: this is specifically access_token, not the entire tokenDetails).
         */
        function getAccessTokenPromise() {
            /* Make sure that tokenDetailsPromise will be a promise to a non-expired token.
             * Note that the promise may not yet be delivered. */
            ensureTokenDetailsPromiseFresh()

            return makeAccessTokenPromise()
        }

        /*
         * Make the promise which just extracts the access_token from the tokenDetailsPromise.
         */
        function makeAccessTokenPromise() {
            return tokenDetailsPromise
                .then(function(tokenDetails) {
                    return tokenDetails.access_token
                })
        }

        /*
         * Ensures that tokenDetailsPromise points to a promise which will have a non-expired token
         * delivered to it. Note that when this method completes the promise may not have yet been delivered upon.
         */
        function ensureTokenDetailsPromiseFresh() {
            var currentTime = new Date()

            /* We only need to update token details if the current one is expired and we're not already in the process of refreshing.
             * The order of the arguments to the && operator is important because refreshTime does not have an initial value. */
            if (!isTheTokenCurrentlyInTheProcessOfBeingRefreshed
                    && currentTime >= refreshTime) {
                isTheTokenCurrentlyInTheProcessOfBeingRefreshed = true

                tokenDetailsPromise = tokenDetailsPromise
                    .then(refreshTokenFn)
                    .done(handleIncomingTokenDetails)
            }
        }

        /*
         * Update the refreshTime variable.
         */
        function updateRefreshTime(tokenDetails) {
            var expiresInSeconds = tokenDetails.expires_in
            var secondsUntilRefreshTime = expiresInSeconds - REFRESH_THRESHOLD_SECONDS
            var millisecondsUntilRefreshTime = secondsUntilRefreshTime * 1000

            refreshTime = new Date(new Date().getTime() + millisecondsUntilRefreshTime)
        }

        /*
         * Handle new incoming token details.
         * Involves indicating that we are no longer in the process of refreshing and updating the refreshTime.
         */
        function handleIncomingTokenDetails(tokenDetails) {
            isTheTokenCurrentlyInTheProcessOfBeingRefreshed = false
            updateRefreshTime(tokenDetails)
        }

        /*
         * Get the base URL of the entellitrak system.
         * Does not contain a trailing /. May return empty string for the current site.
         */
        function getUrl() {
            return url
        }

        return {
            getUrl: getUrl,
            getAccessTokenPromise: getAccessTokenPromise
        }
    }

    /*
     * Get a leading URL fragment.
     * This is a URL which can have another URL with no leading / appended to it and
     * still be valid.
     * The reason this is needed is because inserting a / between URL fragments does
     * not work in the case when the first URL is the empty string.
     */
    function getLeadingUrlFragment(url) {
        if (url === "") {
            return ""
        } else {
            return url + "/"
        }
    }

    /*
     * Calls the refresh token endpoint
     *
     * url is the URL of the entellitrak system and should not contain a trailing /.
     * It may be empty to specify the current system.
     *
     * Returns a promise containing the results of calling the refresh token endpoint
     */
    function refreshOauthToken(url, clientId, refreshToken) {
        var clientIdWithColon = clientId + ":"
        var encodedAuthKey = btoa(clientIdWithColon)

        return jQuery.ajax({
            type: "POST",
            url: getLeadingUrlFragment(url) + NEW_TOKEN_URL,
            headers: {
                Authorization: "Basic " + encodedAuthKey
            },
            data: {
                grant_type: "refresh_token",
                refresh_token: refreshToken
            },
            dataType: "json"
        }).fail(console.error.bind(null, "Token Access Failure:"))
    }

    /*
     * Memoize function.
     * Currently only works for no-argument functions which do not return undefined.
     */
    function memoize(func) {
        var cachedValue

        return function() {
            if (cachedValue === undefined) {
                cachedValue = func()
            }
            return cachedValue
        }
    }

    /*
     * Get an authorization for the current user for the current system..
     */
    var getCurrentUserAuth = memoize(function() {

        /*
         * Function to refresh the auth token details for this client given the previous token details.
         */
        function refreshOauthTokenForClient() {
            /* Effectively, never expire */
            return jQuery.Deferred().resolve({"expires_in": 100000000}).promise()
        }

        return makeAutomaticTokenRefresher(
            "",
            refreshOauthTokenForClient(),
            refreshOauthTokenForClient)
    })

    /*
     * Get token details for a grant_type of client_credentials.
     *
     * url is the URL of the entellitrak system with no trailing /.
     * May be empty to specify the current system.
     * Returns a promise containing the tokenDetails.
     */
    function getClientCredentialsAuthToken(url, clientId, clientSecret) {
        var encodedAuthKey = btoa(clientId + ":" + clientSecret)

        return jQuery.ajax({
                type: "POST",
                url: getLeadingUrlFragment(url) + NEW_TOKEN_URL,
                headers: {
                    Authorization: "Basic " + encodedAuthKey
                },
                data: {
                    grant_type: "client_credentials"
                },
                dataType: "json"
            })
            .fail(console.error.bind(null, "Token Access Failure:"))
    }

    /*
     * Get an authorization for a grant_type of client_credentials.
     * url is the URL of the entellitrak system with no trailing /.
     * May be empty to specify the current system.
     */
    function getClientCredentialsAuth(url, clientId, clientSecret) {
        /* Note that client_credentials do not have refresh tokens. They just fetch new tokens,
         * that is why fetching the initial and subsequent tokens are the same and do not rely on previous tokenDetails.*/

        /*
         * Function to get the tokenDetails for this client.
         */
        function getClientCredentialsAuthTokenForClient() {
            return getClientCredentialsAuthToken(url, clientId, clientSecret)
        }

        return makeAutomaticTokenRefresher(
            url,
            getClientCredentialsAuthTokenForClient(),
            getClientCredentialsAuthTokenForClient)
    }

    /*
     * Get token details for a grant_type password using local username/password
     *
     * url is the URL of the entellitrak system with no trailing /.
     * May be empty to specify the current system.
     * Returns a promise containing the tokenDetails.
     */
    function getUsernamePasswordAuthToken(url, clientId, username, password) {
        var clientIdWithColon = clientId + ":"
        var encodedAuthKey = btoa(clientIdWithColon)

        return jQuery.ajax({
                type: "POST",
                url: getLeadingUrlFragment(url) + NEW_TOKEN_URL,
                headers: {
                    Authorization: "Basic " + encodedAuthKey
                },
                data: {
                    grant_type: "password",
                    username: username,
                    password: password
                },
                dataType: "json"
            })
            .fail(console.error.bind(null, "Token Access Failure:"))
    }

    /*
     * Get an authorization promise using username/password.
     * This method should almost never be used. Prefer current user, or client id/secret over this method.
     * The systemClientId must be the client id of the entellitrak system or else authentication will fail.
     * url is the URL of the entellitrak system with no trailing /.
     * May be empty to specify the current system.
     */
    function getUsernamePasswordAuth(url, systemClientId, username, password) {
        /*
         * Function to refresh the auth token details for this client given the previous token details.
         */
        function refreshOauthTokenForClient(tokenDetails) {
            return refreshOauthToken(url, systemClientId, tokenDetails.refresh_token)
        }

        return makeAutomaticTokenRefresher(
            url,
            getUsernamePasswordAuthToken(url, systemClientId, username, password),
            refreshOauthTokenForClient)
    }

    /*
     * Get the authorization headers given a token
     * The token will be undefined for current user auths.
     */
    function getAuthorizationHeaders(token) {
	    return token === undefined ? {} : {Authorization: "Bearer " + token}
    }

    /*
     * Wrapper around jQuery's ajax method which adds the authorization header
     * and calculated URL.
     *
     * url is the relative URL fragment corresponding to the endpoint. Should not include the base entellitrak
     * URL or api/endpoints.
     * config object will be modified to add the authorization header and URL.
     */
    function ajax(auth, url, config) {
        return auth.getAccessTokenPromise()
            .then(function(accessToken) {
                var calculatedUrl = getLeadingUrlFragment(auth.getUrl()) + "api/endpoints/" + url

                config.url = calculatedUrl

                if (!("headers" in config)) {
                    config.headers = {}
                }

                var authorizationHeader = getAuthorizationHeaders(accessToken)

                jQuery.extend(config.headers, authorizationHeader)

                return jQuery.ajax(config)
            })
    }

    /*
     * Function to get the active workspace name
     *
     * () -> Promise<String>
     */
    var getActiveWorkspaceName = memoize(function() {
        return jQuery.get("page.request.do?page=" + encodeURIComponent(WORKSPACE_PAGE_BUSINESS_KEY))
    })

    /*
     * Convert a workspace name into that workspace name's URL fragment.
     * This is needed because the system workspace behaves differently than all others.
     * URL fragment will end in "/" if necessary.
     */
    function getWorkspaceUrl(workspaceName) {
        var returnValue

        if (workspaceName === "system") {
            returnValue = ""
        } else {
            returnValue = workspaceName + "/"
        }

        return returnValue
    }

    /*
     * Make an ajax request to an endpoint in the current system against the active workspace.
     *
     * auth: an auth
     * url: the URL of the endpoint EXCLUDING api/endpoints/<workspace>
     * config: a config (which will be modified) to be passed to jQuery.ajax()
     */
    function ajaxActiveWorkspace(auth, url, config) {
        return getActiveWorkspaceName()
            .then(function(activeWorkspaceName) {
                var workspaceUrl = getWorkspaceUrl(activeWorkspaceName)
                var calculatedUrl = workspaceUrl + url

                return ajax(auth, calculatedUrl, config)
            })
    }

    /*
     * Make an ajax request to an endpoint in the current system as the current user
     * against their current active workspace.
     *
     * url: the URL of the endpoint EXCLUDING api/endpoints/<workspace>
     * config: a config (which will be modified) to be passed to jQuery.ajax()
     */
    function ajaxCurrentUser(url, config) {
        return ajaxActiveWorkspace(getCurrentUserAuth(), url, config)
    }

    return {
        getCurrentUserAuth: getCurrentUserAuth,
        getClientCredentialsAuth: getClientCredentialsAuth,
        getUsernamePasswordAuth: getUsernamePasswordAuth,

        ajax: ajax,
        ajaxActiveWorkspace: ajaxActiveWorkspace,
        ajaxCurrentUser: ajaxCurrentUser
    }
}(jQuery))