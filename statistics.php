<?php
// Tell PHP that we're using UTF-8 strings until the end of the script
mb_internal_encoding ( 'UTF-8' );

// Tell PHP that we'll be outputting UTF-8 to the browser
mb_http_output ( 'UTF-8' );

$mysql_settings = parse_ini_file ( '/data/project/replacer/replica.my.cnf', false );

$servername = "tools-db";
$username = $mysql_settings ["user"];
$password = $mysql_settings ["password"];
$dbname = "s52978__replacer";

// Create connection
$conn = new mysqli ( $servername, $username, $password, $dbname );
$conn->set_charset ( "utf8mb4" );

// Check connection
if ($conn->connect_error) {
	die ( "Connection failed: " . $conn->connect_error );
}

// Num. Misspellings
$sql = "SELECT COUNT(id) AS num FROM replacement WHERE dtfixed IS NULL";
$result = $conn->query ( $sql );
$numMisspellings = 0;
if ($result->num_rows > 0) {
	while ( $row = $result->fetch_assoc () ) {
		$numMisspellings = $row["num"];
	}
}

// Num. Articles
$sql = "SELECT COUNT(DISTINCT title) AS num FROM replacement WHERE dtfixed IS NULL";
$result = $conn->query ( $sql );
$numTitles = 0;
if ($result->num_rows > 0) {
	while ( $row = $result->fetch_assoc () ) {
		$numTitles = $row["num"];
	}
}

$conn->close ();
?>

<!DOCTYPE html>
<html lang="es">
<head>
<meta charset="UTF-8">
<meta http-equiv="X-UA-Compatible" content="IE=edge">
<meta name="viewport" content="width=device-width, initial-scale=1">
<!-- The above 3 meta tags *must* come first in the head; any other head content must come *after* these tags -->
<title>Replacer Tool - Estadísticas</title>

<!-- Bootstrap -->
<link href="css/bootstrap.min.css" rel="stylesheet">

<style>
body {
	padding-top: 70px;
}
</style>

</head>

<body>

	<nav class="navbar navbar-default navbar-fixed-top">
		<div class="container-fluid">
			<div class="navbar-header">
				<a class="navbar-brand" href="index.php">Replacer</a>
			</div>
		</div>
	</nav>

	<div class="container">

		<h1>Número de posibles errores ortográficos</h1>
		<p><?php echo $numMisspellings; ?> posibles errores.</p>

		<h1>Número de artículos posibles errores ortográficos</h1>
		<p><?php echo $numTitles; ?> artículos.</p>

    </div>

</body>
</html>