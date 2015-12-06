<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<!--
Design by Free CSS Templates
http://www.freecsstemplates.org
Released for free under a Creative Commons Attribution 2.5 License

Name       : Timeless   
Description: A two-column, fixed-width design with dark color scheme.
Version    : 1.0
Released   : 20110825

-->
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta name="keywords" content="" />
<meta name="description" content="" />
<meta http-equiv="content-type" content="text/html; charset=utf-8" />
<title>Illinois Math Solver</title>
<link href="style.css" rel="stylesheet" type="text/css" media="screen" />
</head>
<body>

<?php

require_once 'curl.php';
require_once 'helper.php';

class xmlrpc_client {
    private $url;
    function __construct($url, $autoload=true) {
        $this->url = $url;
        $this->connection = new curl;
        $this->methods = array();
        if ($autoload) {
            $resp = $this->call('system.listMethods', null);
            $this->methods = $resp;
        }
    }
    public function call($method, $params = null) {
        $post = xmlrpc_encode_request($method, $params);
        return xmlrpc_decode($this->connection->post($this->url, $post));
    }
}
?>

<div id="wrapper">
  <div id="header-wrapper">
    <div id="header">
      <div id="logo">
      	<br \><br \>
	    <h1><a href="#">Math Word Problems</a></h1>
      </div>
    </div>
  </div>
    <!-- end #header -->
    <div id="menu">
    <ul>
    <li><a href="index.php">View / Create Folds</a></li>
    <li><a href="addsingle.php">Add a Problem</a></li>
    <li><a href="adddataset.php">Add a Dataset</a></li>
    </ul>
    </div>
    <!-- end #menu -->
  <div id="page">
    <div id="page-bgtop"> 
	  <div id="page-bgbtm"> 
	  	<div id="content">
	      <div class="entry">
	      	<div style="width: 1000px; float: left;">
            	  <form method="post" 
            	  action="<?php echo htmlspecialchars($_SERVER['PHP_SELF']);?>">
              Dataset <input type="text" name="dataset" size="10">
              &nbsp; &nbsp; &nbsp; Lexical Overlap <input type="text" name="lexicalOverlap" 
              size="10">
              &nbsp; &nbsp; &nbsp; Template Overlap <input type="text" name="templateOverlap" 
              size="10">
              &nbsp; &nbsp; &nbsp; <input type="submit" style="width:100px" value="View" size="10">
            	  </form>
			  <br \><br \>
<?php
$rpc = "http://localhost:8082"; 
$client = new xmlrpc_client($rpc, true);
$resp = $client->call('sample.viewFolds', array($_POST['dataset'],
		$_POST['lexicalOverlap'],$_POST['templateOverlap']));
$resp = nl2br(str_replace(" ",'&nbsp&nbsp&nbsp&nbsp',$resp));
echo $resp;

?>
           </div>
          </div> 
	    </div>
	    <div style="clear: both;">&nbsp;</div>
	  </div>
	  <!-- end #content -->
	  <div style="clear: both;">&nbsp;</div>
	</div>
    </div> 
    </div>
    <!-- end #page -->
<div id="footer">
  <p></p>
</div>
<!-- end #footer -->
</body>
</html>
