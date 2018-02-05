<?php
	$hostname_localhost ="localhost";
	$database_localhost ="phototrack";
	$username_localhost ="root";
	$password_localhost ="";

	$localhost = mysql_connect($hostname_localhost,$username_localhost,$password_localhost)
	or
	trigger_error(mysql_error(),E_USER_ERROR);

	mysql_select_db($database_localhost, $localhost);

	$path=$_POST['path'];
	$private=$_POST['private'];
	$timestamp=$_POST['timestamp'];

	$insert = "INSERT INTO photos(path,private,timestamp) VALUES ('".$path."','".$private."','".$timestamp."')";
	
	$query_exec = mysql_query($insert) 
	or 
	die(mysql_error());
?>
