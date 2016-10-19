<?php

// Load the user token (request or access) from the session
$gTokenKey = '';
session_start ();
if (isset ( $_SESSION ['tokenKey'] )) {
	$gTokenKey = $_SESSION ['tokenKey'];
}
session_write_close ();

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

	<nav id="cabecera" class="navbar navbar-default navbar-fixed-top">
		<div class="container-fluid">
			<div class="navbar-header">
				<a class="navbar-brand" href="index.php">Replacer</a>
			</div>
			<ul class="nav navbar-nav">
				<li><a id="link-title" href="#" target="_blank"></a></li>
			</ul>
			<ul id="ul-save" class="nav navbar-nav navbar-right collapse">
				<li><a id="button-save" href="#" class="btn btn-default"
					role="button">Guardar cambios</a></li>
			</ul>
		</div>
	</nav>

	<div class="container">

		<input id="tokenKey" type="hidden" value="<?php echo $gTokenKey; ?>" />

		<?php if ( !$gTokenKey ) { ?>

		<p>Haga clic <a href="index.php?action=authorize">aquí</a> para autenticarse.</p>

		<?php } else { ?>

		<div id="article-content" class="pre"></div>

		<?php } ?>

    </div>

	<!-- jQuery (necessary for Bootstrap's JavaScript plugins) -->
	<script src="https://ajax.googleapis.com/ajax/libs/jquery/1.11.3/jquery.min.js"></script>
	<!-- Include all compiled plugins (below), or include individual files as needed -->
	<script src="js/bootstrap.min.js"></script>

	<script src="js/string-utils.js"></script>
	<script src="js/regex.js"></script>
	<script src="js/replace-utils.js"></script>
	<script src="js/db-utils.js"></script>
	<script src="js/wikipedia-utils.js"></script>
	<script src="js/replacer.js"></script>

</body>
</html>
