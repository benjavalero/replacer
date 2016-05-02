<?php
/**
 * GET page from Spanish Wikipedia
 * Parameters:
 *    title : Title of the page to retrieve
 */

// GET parameters
$title = htmlspecialchars($_GET["title"]);

// Set up the curl resource
$ch = curl_init();
curl_setopt($ch, CURLOPT_URL, "https://es.wikipedia.org/w/api.php?action=query&titles=$title&prop=revisions&rvprop=content&format=json");
curl_setopt($ch, CURLOPT_RETURNTRANSFER, 1);
curl_setopt($ch, CURLOPT_HEADER, 0);

// Execute the request
$output = curl_exec($ch);

// Output the profile information - includes the header
header("Content-type: application/json");
echo($output) . PHP_EOL;

// Close curl resource to free up system resources
curl_close($ch);