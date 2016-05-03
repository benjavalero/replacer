<?php
/**
 * POST login to Spanish Wikipedia
 * Parameters:
 *    name : User name
 *    password  : User password
 */

// POST parameters
$lgname = htmlspecialchars($_GET["name"]);
$lgpswd = htmlspecialchars($_GET["password"]);

$url = "https://es.wikipedia.org/w/api.php";
$data = "action=login&format=json&lgname=$lgname&lgpassword=$lgpswd";

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
header("Content-type: application/json");
echo($output) . PHP_EOL;

// Close curl resource to free up system resources
curl_close($ch);
