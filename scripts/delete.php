<?php
	$hostname_localhost ="localhost";
	$database_localhost ="phototrack";
	$username_localhost ="root";
	$password_localhost ="";

	$localhost = mysql_connect($hostname_localhost,$username_localhost,$password_localhost)
	or
	trigger_error(mysql_error(),E_USER_ERROR);

	mysql_select_db($database_localhost, $localhost);

	$id = $_POST['id'];

	$delete = "DELETE FROM user WHERE id = '$id'";
	
	$query_exec = mysql_query($delete) 
	or 
	die(mysql_error());

?>