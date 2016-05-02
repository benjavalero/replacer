<?php
/**
 * POST page to Spanish Wikipedia
 * Parameters:
 *    title : Title of the page to update
 *    text  : Text of the page to update
 */

// POST parameters
$title = htmlspecialchars($_GET["title"]);
$text = htmlspecialchars($_GET["text"]);

$url = "https://es.wikipedia.org/w/api.php";
$data = "action=edit&title=$title&text=$text&token=%2B%5C";

// JSON encode data
// $data_string = urlencode($data);

// Set up the curl resource
$ch = curl_init();
curl_setopt($ch, CURLOPT_URL, $url);
curl_setopt($ch, CURLOPT_POST, 1);
curl_setopt($ch, CURLOPT_RETURNTRANSFER, true);
curl_setopt($ch, CURLOPT_POSTFIELDS, $data);
curl_setopt($ch, CURLOPT_HTTPHEADER, array("Content-Type: application/x-www-form-urlencoded"));

// Execute the request
$output = curl_exec($ch);

// Output the profile information - includes the header
echo($output) . PHP_EOL;

// Close curl resource to free up system resources
curl_close($ch);