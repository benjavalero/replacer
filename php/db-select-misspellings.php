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
$sql = "SELECT m.word, m.cs, m.suggestion FROM replacement r, misspelling m WHERE r.word = m.word AND r.title = '$title'";
$result = $conn->query($sql);
$misspelling_array = array();
if ($result->num_rows > 0) {
	while($row = $result->fetch_assoc()) {
		$misspelling = array(
			"word" => $row["word"],
			"cs" => $row["cs"],
			"suggestion" => $row["suggestion"]
		);
		array_push($misspelling_array, $misspelling);
	}
}

$data = array("misspellings" => $misspelling_array);
echo json_encode($data, JSON_UNESCAPED_UNICODE);

$conn->close();

header("Content-type: application/json; charset=UTF-8");
?>
