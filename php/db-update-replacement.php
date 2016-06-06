<?php
// Tell PHP that we're using UTF-8 strings until the end of the script
mb_internal_encoding('UTF-8');

// Tell PHP that we'll be outputting UTF-8 to the browser
mb_http_output('UTF-8');

$mysql_settings = parse_ini_file('/data/project/replacer/replica.my.cnf', false);

$servername = "tools-db";
$username = $mysql_settings["user"];
$password = $mysql_settings["password"];
$dbname = "s52978__replacer";

// Create connection
$conn = new mysqli($servername, $username, $password, $dbname);
$conn->set_charset("utf8mb4");

// Check connection
if ($conn->connect_error) {
	die("Connection failed: " . $conn->connect_error);
}

$title = $conn->real_escape_string($_GET["title"]);
$sql = "UPDATE replacement SET dtfixed = NOW() WHERE title = '$title'";
$results = $conn->query($sql);

if ($results) {
	$data = array("success" => $conn->affected_rows);
	echo json_encode($data, JSON_UNESCAPED_UNICODE);
} else {
	$data = array("error" => $conn->error);
	echo json_encode($data, JSON_UNESCAPED_UNICODE);
}

$conn->close();

header("Content-type: application/json; charset=UTF-8");
?>
