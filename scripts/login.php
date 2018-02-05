<?php
	$hostname_localhost ="localhost";
	$database_localhost ="phototrack";
	$username_localhost ="root";
	$password_localhost ="";

	$localhost = mysql_connect($hostname_localhost,$username_localhost,$password_localhost)
	or
	trigger_error(mysql_error(),E_USER_ERROR);

	mysql_select_db($database_localhost, $localhost);

	$username=$_POST['username'];
	$password=$_POST['password'];

	$login = "SELECT id FROM user WHERE username = '$username' AND password = '$password'";
	
	$query_exec = mysql_query($login)
	or 
	die(mysql_error());
?>