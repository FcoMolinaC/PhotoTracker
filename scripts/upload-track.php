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

<<<<<<< HEAD
	$insert = "INSERT INTO tracks(track,private) VALUES ('".$track."','".$private."')";
=======
	$insert = "INSERT INTO photos(track,private) VALUES ('".$track."','".$private."')";
>>>>>>> 56335eb7cddf510f4467ef5c0dcaa18108a6bd42
	
	$query_exec = mysql_query($insert) 
	or 
	die(mysql_error());
?>
