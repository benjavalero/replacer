<?php
/**
 * Written in 2013 by Brad Jorsch
 *
 * To the extent possible under law, the author(s) have dedicated all copyright 
 * and related and neighboring rights to this software to the public domain 
 * worldwide. This software is distributed without any warranty. 
 *
 * See <http://creativecommons.org/publicdomain/zero/1.0/> for a copy of the 
 * CC0 Public Domain Dedication.
 */

// ******************** CONFIGURATION ********************

/**
 * Set this to point to a file (outside the webserver root!) containing the
 * following keys:
 * - agent: The HTTP User-Agent to use
 * - consumerKey: The "consumer token" given to you when registering your app
 * - consumerSecret: The "secret token" given to you when registering your app
 */
$inifile = '/data/project/replacer/oauth.ini';

/**
 * Set this to the Special:OAuth/authorize URL.
 *
 * To work around MobileFrontend redirection, use /wiki/ rather than /w/index.php.
 */
$mwOAuthAuthorizeUrl = 'https://meta.wikimedia.org/wiki/Special:OAuth/authorize';

/**
 * Set this to the Special:OAuth URL.
 *
 * Note that /wiki/Special:OAuth fails when checking the signature, while
 * index.php?title=Special:OAuth works fine.
 */
$mwOAuthUrl = 'https://meta.wikimedia.org/w/index.php?title=Special:OAuth';

/**
 * Set this to the interwiki prefix for the OAuth central wiki.
 */
$mwOAuthIW = 'mw';

/**
 * Set this to the API endpoint
 */
$apiUrl = 'https://es.wikipedia.org/w/api.php';

/**
 * This should normally be "500".
 * But Tool Labs insists on overriding valid 500
 * responses with a useless error page.
 */
$errorCode = 200;

// ****************** END CONFIGURATION ******************

// Setup the session cookie
session_name ( 'ReplacerTool' );
$params = session_get_cookie_params ();
session_set_cookie_params ( $params ['lifetime'], dirname ( $_SERVER ['SCRIPT_NAME'] ) );

// Read the ini file
$ini = parse_ini_file ( $inifile );
if ($ini === false) {
	header ( "HTTP/1.1 $errorCode Internal Server Error" );
	echo 'The ini file could not be read';
	exit ( 0 );
}
if (! isset ( $ini ['agent'] ) || ! isset ( $ini ['consumerKey'] ) || ! isset ( $ini ['consumerSecret'] )) {
	header ( "HTTP/1.1 $errorCode Internal Server Error" );
	echo 'Required configuration directives not found in ini file';
	exit ( 0 );
}
$gUserAgent = $ini ['agent'];
$gConsumerKey = $ini ['consumerKey'];
$gConsumerSecret = $ini ['consumerSecret'];

// Load the user token (request or access) from the session
$gTokenKey = '';
$gTokenSecret = '';
session_start ();
if (isset ( $_SESSION ['tokenKey'] )) {
	$gTokenKey = $_SESSION ['tokenKey'];
	$gTokenSecret = $_SESSION ['tokenSecret'];
}
session_write_close ();

// Fetch the access token if this is the callback from requesting authorization
if (isset ( $_GET ['oauth_verifier'] ) && $_GET ['oauth_verifier']) {
	fetchAccessToken ();
}

// Take any requested action
switch (isset ( $_GET ['action'] ) ? $_GET ['action'] : '') {
	
	case 'authorize' :
		doAuthorizationRedirect ();
		return;
	
	case 'get' :
		getPageContent ();
		return;
}

switch (isset ( $_POST ['action'] ) ? $_POST ['action'] : '') {
	
	case 'edit' :
		doEdit ();
		return;
}

// ******************** CODE ********************

/**
 * Utility function to sign a request
 *
 * Note this doesn't properly handle the case where a parameter is set both in
 * the query string in $url and in $params, or non-scalar values in $params.
 *
 * @param string $method
 *        	Generally "GET" or "POST"
 * @param string $url
 *        	URL string
 * @param array $params
 *        	Extra parameters for the Authorization header or post
 *        	data (if application/x-www-form-urlencoded).
 *        	 @return string Signature
 */
function sign_request($method, $url, $params = array()) {
	global $gConsumerSecret, $gTokenSecret;
	
	$parts = parse_url ( $url );
	
	// We need to normalize the endpoint URL
	$scheme = isset ( $parts ['scheme'] ) ? $parts ['scheme'] : 'http';
	$host = isset ( $parts ['host'] ) ? $parts ['host'] : '';
	$port = isset ( $parts ['port'] ) ? $parts ['port'] : ($scheme == 'https' ? '443' : '80');
	$path = isset ( $parts ['path'] ) ? $parts ['path'] : '';
	if (($scheme == 'https' && $port != '443') || ($scheme == 'http' && $port != '80')) {
		// Only include the port if it's not the default
		$host = "$host:$port";
	}
	
	// Also the parameters
	$pairs = array ();
	parse_str ( isset ( $parts ['query'] ) ? $parts ['query'] : '', $query );
	$query += $params;
	unset ( $query ['oauth_signature'] );
	if ($query) {
		$query = array_combine ( 
				// rawurlencode follows RFC 3986 since PHP 5.3
				array_map ( 'rawurlencode', array_keys ( $query ) ), array_map ( 'rawurlencode', array_values ( $query ) ) );
		ksort ( $query, SORT_STRING );
		foreach ( $query as $k => $v ) {
			$pairs [] = "$k=$v";
		}
	}
	
	$toSign = rawurlencode ( strtoupper ( $method ) ) . '&' . rawurlencode ( "$scheme://$host$path" ) . '&' . rawurlencode ( join ( '&', $pairs ) );
	$key = rawurlencode ( $gConsumerSecret ) . '&' . rawurlencode ( $gTokenSecret );
	return base64_encode ( hash_hmac ( 'sha1', $toSign, $key, true ) );
}

/**
 * Request authorization
 * 
 * @return void
 */
function doAuthorizationRedirect() {
	global $mwOAuthUrl, $mwOAuthAuthorizeUrl, $gUserAgent, $gConsumerKey, $gTokenSecret;
	
	// First, we need to fetch a request token.
	// The request is signed with an empty token secret and no token key.
	$gTokenSecret = '';
	$url = $mwOAuthUrl . '/initiate';
	$url .= strpos ( $url, '?' ) ? '&' : '?';
	$url .= http_build_query ( array (
			'format' => 'json',
			
			// OAuth information
			'oauth_callback' => 'oob', // Must be "oob" for MWOAuth
			'oauth_consumer_key' => $gConsumerKey,
			'oauth_version' => '1.0',
			'oauth_nonce' => md5 ( microtime () . mt_rand () ),
			'oauth_timestamp' => time (),
			
			// We're using secret key signatures here.
			'oauth_signature_method' => 'HMAC-SHA1' 
	) );
	$signature = sign_request ( 'GET', $url );
	$url .= "&oauth_signature=" . urlencode ( $signature );
	$ch = curl_init ();
	curl_setopt ( $ch, CURLOPT_URL, $url );
	// curl_setopt( $ch, CURLOPT_SSL_VERIFYPEER, false );
	curl_setopt ( $ch, CURLOPT_USERAGENT, $gUserAgent );
	curl_setopt ( $ch, CURLOPT_HEADER, 0 );
	curl_setopt ( $ch, CURLOPT_RETURNTRANSFER, 1 );
	$data = curl_exec ( $ch );
	if (! $data) {
		header ( "HTTP/1.1 $errorCode Internal Server Error" );
		echo 'Curl error: ' . htmlspecialchars ( curl_error ( $ch ) );
		exit ( 0 );
	}
	curl_close ( $ch );
	$token = json_decode ( $data );
	if (is_object ( $token ) && isset ( $token->error )) {
		header ( "HTTP/1.1 $errorCode Internal Server Error" );
		echo 'Error retrieving token: ' . htmlspecialchars ( $token->error );
		exit ( 0 );
	}
	if (! is_object ( $token ) || ! isset ( $token->key ) || ! isset ( $token->secret )) {
		header ( "HTTP/1.1 $errorCode Internal Server Error" );
		echo 'Invalid response from token request';
		exit ( 0 );
	}
	
	// Now we have the request token, we need to save it for later.
	session_start ();
	$_SESSION ['tokenKey'] = $token->key;
	$_SESSION ['tokenSecret'] = $token->secret;
	session_write_close ();
	
	// Then we send the user off to authorize
	$url = $mwOAuthAuthorizeUrl;
	$url .= strpos ( $url, '?' ) ? '&' : '?';
	$url .= http_build_query ( array (
			'oauth_token' => $token->key,
			'oauth_consumer_key' => $gConsumerKey 
	) );
	header ( "Location: $url" );
	echo 'Please see <a href="' . htmlspecialchars ( $url ) . '">' . htmlspecialchars ( $url ) . '</a>';
}

/**
 * Handle a callback to fetch the access token
 * 
 * @return void
 */
function fetchAccessToken() {
	global $mwOAuthUrl, $gUserAgent, $gConsumerKey, $gTokenKey, $gTokenSecret;
	
	$url = $mwOAuthUrl . '/token';
	$url .= strpos ( $url, '?' ) ? '&' : '?';
	$url .= http_build_query ( array (
			'format' => 'json',
			'oauth_verifier' => $_GET ['oauth_verifier'],
			
			// OAuth information
			'oauth_consumer_key' => $gConsumerKey,
			'oauth_token' => $gTokenKey,
			'oauth_version' => '1.0',
			'oauth_nonce' => md5 ( microtime () . mt_rand () ),
			'oauth_timestamp' => time (),
			
			// We're using secret key signatures here.
			'oauth_signature_method' => 'HMAC-SHA1' 
	) );
	$signature = sign_request ( 'GET', $url );
	$url .= "&oauth_signature=" . urlencode ( $signature );
	$ch = curl_init ();
	curl_setopt ( $ch, CURLOPT_URL, $url );
	// curl_setopt( $ch, CURLOPT_SSL_VERIFYPEER, false );
	curl_setopt ( $ch, CURLOPT_USERAGENT, $gUserAgent );
	curl_setopt ( $ch, CURLOPT_HEADER, 0 );
	curl_setopt ( $ch, CURLOPT_RETURNTRANSFER, 1 );
	$data = curl_exec ( $ch );
	if (! $data) {
		header ( "HTTP/1.1 $errorCode Internal Server Error" );
		echo 'Curl error: ' . htmlspecialchars ( curl_error ( $ch ) );
		exit ( 0 );
	}
	curl_close ( $ch );
	$token = json_decode ( $data );
	if (is_object ( $token ) && isset ( $token->error )) {
		header ( "HTTP/1.1 $errorCode Internal Server Error" );
		echo 'Error retrieving token: ' . htmlspecialchars ( $token->error );
		exit ( 0 );
	}
	if (! is_object ( $token ) || ! isset ( $token->key ) || ! isset ( $token->secret )) {
		header ( "HTTP/1.1 $errorCode Internal Server Error" );
		echo 'Invalid response from token request';
		exit ( 0 );
	}
	
	// Save the access token
	session_start ();
	$_SESSION ['tokenKey'] = $gTokenKey = $token->key;
	$_SESSION ['tokenSecret'] = $gTokenSecret = $token->secret;
	session_write_close ();
}

/**
 * Send an API query with OAuth authorization
 *
 * @param array $post
 *        	Post data
 * @param object $ch
 *        	Curl handle
 * @return array API results
 */
function doApiQuery($post, &$ch = null) {
	global $apiUrl, $gUserAgent, $gConsumerKey, $gTokenKey;
	
	$headerArr = array (
			// OAuth information
			'oauth_consumer_key' => $gConsumerKey,
			'oauth_token' => $gTokenKey,
			'oauth_version' => '1.0',
			'oauth_nonce' => md5 ( microtime () . mt_rand () ),
			'oauth_timestamp' => time (),
			
			// We're using secret key signatures here.
			'oauth_signature_method' => 'HMAC-SHA1' 
	);
	$signature = sign_request ( 'POST', $apiUrl, $post + $headerArr );
	$headerArr ['oauth_signature'] = $signature;
	
	$header = array ();
	foreach ( $headerArr as $k => $v ) {
		$header [] = rawurlencode ( $k ) . '="' . rawurlencode ( $v ) . '"';
	}
	$header = 'Authorization: OAuth ' . join ( ', ', $header );
	
	if (! $ch) {
		$ch = curl_init ();
	}
	curl_setopt ( $ch, CURLOPT_POST, true );
	curl_setopt ( $ch, CURLOPT_URL, $apiUrl );
	curl_setopt ( $ch, CURLOPT_POSTFIELDS, http_build_query ( $post ) );
	curl_setopt ( $ch, CURLOPT_HTTPHEADER, array (
			$header 
	) );
	// curl_setopt( $ch, CURLOPT_SSL_VERIFYPEER, false );
	curl_setopt ( $ch, CURLOPT_USERAGENT, $gUserAgent );
	curl_setopt ( $ch, CURLOPT_HEADER, 0 );
	curl_setopt ( $ch, CURLOPT_RETURNTRANSFER, 1 );
	$data = curl_exec ( $ch );
	if (! $data) {
		header ( "HTTP/1.1 $errorCode Internal Server Error" );
		echo 'Curl error: ' . htmlspecialchars ( curl_error ( $ch ) );
		exit ( 0 );
	}
	
	// $ret = json_decode( $data );
	$ret = $data;
	
	if ($ret === null) {
		header ( "HTTP/1.1 $errorCode Internal Server Error" );
		echo 'Unparsable API response: <pre>' . htmlspecialchars ( $data ) . '</pre>';
		exit ( 0 );
	}
	return $ret;
}

/**
 * Get the content of a Wikipedia page
 */
function getPageContent() {
	$ch = null;
	
	// First fetch the username
	$res = doApiQuery ( array (
			'format' => 'json',
			'action' => 'query',
			'prop' => 'revisions',
			'rvprop' => 'content',
			'titles' => $_GET ["title"] 
	), $ch );
	
	header ( "Content-type: application/json" );
	echo $res;
}

/**
 * Perform a generic edit
 * 
 * @return void
 */
function doEdit() {
	global $mwOAuthIW;
	
	$ch = null;
	
	// Next fetch the edit token
	$res = doApiQuery ( array (
			'format' => 'json',
			'action' => 'tokens',
			'type' => 'edit' 
	), $ch );
	
	$resDecoded = json_decode ( $res, true );
	$token = $resDecoded ["tokens"] ["edittoken"];
	if (! isset ( $token )) {
		header ( "HTTP/1.1 $errorCode Internal Server Error" );
		echo 'Bad API response: <pre>' . htmlspecialchars ( var_export ( $res, 1 ) ) . '</pre>';
		exit ( 0 );
	}
	
	// Now perform the edit
	$res = doApiQuery ( array (
			'format' => 'json',
			'action' => 'edit',
			'title' => $_POST ["title"],
			'text' => $_POST ["text"],
			'summary' => 'Correcciones ortográficas',
			'minor' => 'true',
			'token' => $token 
	), $ch );
	
	echo 'API edit result: <pre>' . htmlspecialchars ( var_export ( $res, 1 ) ) . '</pre>';
	echo '<hr>';
}

// ******************** WEBPAGE ********************

?>
<!DOCTYPE html>
<html lang="es">
<head>
<meta charset="UTF-8">
<meta http-equiv="X-UA-Compatible" content="IE=edge">
<meta name="viewport" content="width=device-width, initial-scale=1">
<!-- The above 3 meta tags *must* come first in the head; any other head content must come *after* these tags -->
<title>Replacer Tool</title>

<!-- Bootstrap -->
<link href="css/bootstrap.min.css" rel="stylesheet">

<link href="css/replacer.css" rel="stylesheet">

<!-- HTML5 shim and Respond.js for IE8 support of HTML5 elements and media queries -->
<!-- WARNING: Respond.js doesn't work if you view the page via file:// -->
<!--[if lt IE 9]>
      <script src="https://oss.maxcdn.com/html5shiv/3.7.2/html5shiv.min.js"></script>
      <script src="https://oss.maxcdn.com/respond/1.4.2/respond.min.js"></script>
    <![endif]-->
</head>

<body>

	<nav class="navbar navbar-default navbar-fixed-top">
		<div class="container-fluid">
			<div class="navbar-header">
				<a class="navbar-brand" href="#">Replacer</a>
			</div>
			<ul id="ul-save" class="nav navbar-nav navbar-right collapse">
				<li><a id="button-save" href="#" class="btn btn-default"
					role="button">Guardar cambios</a></li>
			</ul>
		</div>
	</nav>

	<div class="container">

		<p>
			Esta es una herramienta aún en fase <strong>beta</strong> para
			corregir los errores ortográficos más comunes en la Wikipedia en
			español.
		</p>

		<p>Pulse en las palabras resaltadas para sustituirlas por su
			corrección (si procede). Finalmente pulse en el botón "Guardar
			cambios" para aplicar las correcciones. Si pulsa el botón sin haber
			hecho ningún cambio, simplemente se cargará una nueva página.</p>

		<input id="tokenKey" type="hidden" value="<?php echo $gTokenKey; ?>" />
      <?php if ( !$gTokenKey ) { ?>
      <p>
			Haga clic <a href="index.php?action=authorize">aquí</a> para
			autenticarse.
		</p>
      <?php } else { ?>

      <h2>
			<span id="pageTitle"></span>
		</h2>

		<div id="article-content" class="pre"></div>

		<!-- Para depuración -->
		<div>
			<button id="button-show-changes" class="btn btn-default collapse"
				type="button">Mostrar cambios</button>
		</div>
		<div id="content-to-post" class="pre collapse"></div>

      <?php } ?>

    </div>

	<!-- jQuery (necessary for Bootstrap's JavaScript plugins) -->
	<script
		src="https://ajax.googleapis.com/ajax/libs/jquery/1.11.3/jquery.min.js"></script>
	<!-- Include all compiled plugins (below), or include individual files as needed -->
	<script src="js/bootstrap.min.js"></script>

	<script src="js/replacer.js"></script>
</body>
</html>
