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
$sql = "SELECT m.word, m.cs, m.suggestion FROM replacement r, misspelling m WHERE r.word = m.word AND r.title = '$title'";
$result = $conn->query($sql);
$misspelling_array = array();
if ($result->num_rows > 0) {
	while($row = $result->fetch_assoc()) {
		$misspelling = array(
			"word" => utf8_encode($row["word"]),
			"cs" => $row["cs"],
			"suggestion" => utf8_encode($row["suggestion"])
		);
		array_push($misspelling_array, $misspelling);
	}
}

header("Content-type: application/json");

$data = array("misspellings" => $misspelling_array);
echo json_encode($data, JSON_UNESCAPED_UNICODE);

$conn->close();
?>
