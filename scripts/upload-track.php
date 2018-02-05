<?php
	$hostname_localhost ="localhost";
	$database_localhost ="phototrack";
	$username_localhost ="root";
	$password_localhost ="";

	$localhost = mysql_connect($hostname_localhost,$username_localhost,$password_localhost)
	or
	trigger_error(mysql_error(),E_USER_ERROR);

	mysql_select_db($database_localhost, $localhost);

	$track=$_POST['track'];
	$private=$_POST['private'];

	$insert = "INSERT INTO photos(track,private) VALUES ('".$track."','".$private."')";
	
	$query_exec = mysql_query($insert) 
	or 
	die(mysql_error());
?>
