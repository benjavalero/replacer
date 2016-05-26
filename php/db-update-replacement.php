<?php
$mysql_settings = parse_ini_file('/data/project/replacer/replica.my.cnf', false);

$servername = "tools-db";
$username = $mysql_settings["user"];
$password = $mysql_settings["password"];
$dbname = "s52978__replacer";

// Create connection
$conn = new mysqli($servername, $username, $password, $dbname);
// Check connection
if ($conn->connect_error) {
	die("Connection failed: " . $conn->connect_error);
}

$title = $conn->real_escape_string(utf8_decode($_GET["title"]));
$sql = "UPDATE replacement SET dtfixed = NOW() WHERE title = '$title'";
$results = $conn->query($sql);

header("Content-type: application/json");

if ($results) {
	$data = array("success" => $conn->affected_rows);
	echo json_encode($data, JSON_UNESCAPED_UNICODE);
} else {
	$data = array("error" => $conn->error);
	echo json_encode($data, JSON_UNESCAPED_UNICODE);
}

$conn->close();
?>
