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

$title = mysql_real_escape_string(utf8_decode($_POST["title"]));
$sql = "UPDATE replacement SET dtfixed = NOW() WHERE title = '$title'";

header("Content-type: application/json");

if (!$conn->query($sql) === TRUE) {
	$data = array("error" => $conn->error);
	echo json_encode($data, JSON_UNESCAPED_UNICODE);
}

$conn->close();
?>
