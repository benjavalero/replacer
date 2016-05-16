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

$sql = "SELECT title, COUNT(*) FROM replacement WHERE dtfixed = 0 GROUP BY title ORDER BY COUNT(*) ASC LIMIT 10";
$result = $conn->query($sql);
if ($result->num_rows > 0) {
    $title_array = array();
    while($row = $result->fetch_assoc()) {
        array_push($title_array, utf8_encode($row["title"]));
    }
}

$data = array("titles" => $title_array);
echo json_encode($data, JSON_UNESCAPED_UNICODE);

/*
$sql = "SELECT m.word, m.cs, m.suggestion FROM replacement r, misspelling m WHERE r.word = m.word AND r.title = '$title'";
$result = $conn->query($sql);
if ($result->num_rows > 0) {
    while($row = $result->fetch_assoc()) {
        echo "{\"word\":\"" . $row["word"] . "\",\"cs\":\"" . $row["cs"] . "\",\"suggestion\":\"" . $row["suggestion"] . "\"},";
    }
}
*/
header("Content-type: application/json");

$conn->close();
?>
