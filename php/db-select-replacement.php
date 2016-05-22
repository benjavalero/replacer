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

$sql = "SELECT DISTINCT title FROM replacement WHERE dtfixed = 0 LIMIT 10";
$result = $conn->query($sql);
if ($result->num_rows > 0) {
	$title_array = array();
	while($row = $result->fetch_assoc()) {
		array_push($title_array, utf8_encode($row["title"]));
	}
}

header("Content-type: application/json");

$data = array("titles" => $title_array);
echo json_encode($data, JSON_UNESCAPED_UNICODE);

$conn->close();
?>
