<?php
/**
 * RESTful cURL class
 *
 * This is a wrapper class for curl, it is quite easy to use:
 * <code>
 * $c = new curl;
 * // enable cache
 * $c = new curl(array('cache'=>true));
 * // enable cookie
 * $c = new curl(array('cookie'=>true));
 * // enable proxy
 * $c = new curl(array('proxy'=>true));
 *
 * // HTTP GET Method
 * $html = $c->get('http://example.com');
 * // HTTP POST Method
 * $html = $c->post('http://example.com/', array('q'=>'words', 'name'=>'moodle'));
 * // HTTP PUT Method
 * $html = $c->put('http://example.com/', array('file'=>'/var/www/test.txt');
 * </code>
 *
 * @copyright  Dongsheng Cai {@see http://dongsheng.org}
 * @license    http://www.gnu.org/copyleft/gpl.html GNU Public License
 */

class curl {
    /** @var bool */
    public  $cache    = false;
    public  $proxy    = false;
    /** @var array */
    public  $response = array();
    public  $headers  = array();
    /** @var string */
    public  $info;
    public  $error;

    /** @var array */
    private $options;
    /** @var string */
    private $proxy_host = '';
    private $proxy_auth = '';
    private $proxy_type = '';
    /** @var bool */
    private $debug    = false;
    private $cookie   = false;

    /**
     * @param array $options
     */
    public function __construct($options = array()) {
        if (!function_exists('curl_init')) {
            throw new Exception('cURL module must be enabled!');
        }
        // the options of curl should be init here.
        $this->initialize_default_options();
        if (!empty($options['debug'])) {
            $this->debug = true;
        }
        if(!empty($options['cookie'])) {
            if($options['cookie'] === true) {
                $this->cookie = 'curl_cookie.txt';
            } else {
                $this->cookie = $options['cookie'];
            }
        }
        if (!empty($options['cache'])) {
            if (class_exists('curl_cache')) {
                $this->cache = new curl_cache();
            }
        }
    }
    /**
     * Resets the CURL options that have already been set
     */
    private function initialize_default_options() {
        $this->options = array();
        $this->options['CURLOPT_USERAGENT']         = 'cURL';
        // True to include the header in the output
        $this->options['CURLOPT_HEADER']            = 0;
        // True to Exclude the body from the output
        $this->options['CURLOPT_NOBODY']            = 0;
        // TRUE to follow any "Location: " header that the server
        // sends as part of the HTTP header (note this is recursive,
        // PHP will follow as many "Location: " headers that it is sent,
        // unless CURLOPT_MAXREDIRS is set).
        //$this->options['CURLOPT_FOLLOWLOCATION']    = 1;
        $this->options['CURLOPT_MAXREDIRS']         = 10;
        $this->options['CURLOPT_ENCODING']          = '';
        // TRUE to return the transfer as a string of the return
        // value of curl_exec() instead of outputting it out directly.
        $this->options['CURLOPT_RETURNTRANSFER']    = 1;
        $this->options['CURLOPT_BINARYTRANSFER']    = 0;
        $this->options['CURLOPT_SSL_VERIFYPEER']    = 0;
        $this->options['CURLOPT_SSL_VERIFYHOST']    = 2;
        $this->options['CURLOPT_CONNECTTIMEOUT']    = 30;
    }

    /**
     * Reset Cookie
     */
    public function delete_cookie() {
        if (!empty($this->cookie)) {
            if (is_file($this->cookie)) {
                $fp = fopen($this->cookie, 'w');
                if (!empty($fp)) {
                    fwrite($fp, '');
                    fclose($fp);
                }
            }
        }
    }

    /**
     * Set curl option
     *
     * @param string $name
     * @param string $value
     */
    public function set_option($name, $value) {
        $this->set_options(array($name, $value));
    }
    /**
     * Set curl options
     *
     * @param array $options If array is null, this function will
     *                       reset the options to default value.
     */
    public function set_options($options = array()) {
        if (is_array($options)) {
            foreach($options as $name => $val){
                if (stripos($name, 'CURLOPT_') === false) {
                    $name = strtoupper('CURLOPT_'.$name);
                }
                $this->options[$name] = $val;
            }
        }
    }
    /**
     * Reset http method
     *
     */
    public function reset_options() {
        unset($this->options['CURLOPT_HTTPGET']);
        unset($this->options['CURLOPT_POST']);
        unset($this->options['CURLOPT_POSTFIELDS']);
        unset($this->options['CURLOPT_PUT']);
        unset($this->options['CURLOPT_INFILE']);
        unset($this->options['CURLOPT_INFILESIZE']);
        unset($this->options['CURLOPT_CUSTOMREQUEST']);
    }

    public function set_header($header) {
        $this->set_headers($header);
    }
    /**
     * Set HTTP Request Header
     *
     * @param array $headers
     */
    public function set_headers($header) {
        if (is_array($header)){
            foreach ($header as $v) {
                $this->set_headers($v);
            }
        } else {
            $this->headers[] = $header;
        }
    }

    /**
     * Set HTTP Response Header
     */
    public function get_response() {
        return $this->response;
    }
    /**
     * private callback function
     * Formatting HTTP Response Header
     *
     * @param mixed $ch Apparently not used
     * @param string $header
     * @return int The strlen of the header
     */
    private function handle_response_headers($ch, $header) {
//        $this->count++;
        if (strlen($header) > 2) {
            list($key, $value) = explode(" ", rtrim($header, "\r\n"), 2);
            $key = rtrim($key, ':');
            if (!empty($this->response[$key])) {
                if (is_array($this->response[$key])){
                    $this->response[$key][] = $value;
                } else {
                    $tmp = $this->response[$key];
                    $this->response[$key] = array();
                    $this->response[$key][] = $tmp;
                    $this->response[$key][] = $value;

                }
            } else {
                $this->response[$key] = $value;
            }
        }
        return strlen($header);
    }

    /**
     * Set options for individual curl instance
     *
     * @param object $curl A curl handle
     * @param array $options
     * @return object The curl handle
     */
    private function apply_options($curl, $options) {
        // set cookie
        if (!empty($this->cookie) || !empty($options['cookie'])) {
            $this->set_option('cookiejar', $this->cookie);
            $this->set_option('cookiefile', $this->cookie);
        }

        // set proxy
        if (!empty($this->proxy) || !empty($options['proxy'])) {
            $this->set_options($this->proxy);
        }
        $this->set_options($options);
        // reset before set options
        curl_setopt($curl, CURLOPT_HEADERFUNCTION, array(&$this,'handle_response_headers'));
        // set headers
        if (empty($this->headers)){
            $this->set_headers(array(
                'User-Agent: ' . $this->options['CURLOPT_USERAGENT'],
                'Accept-Charset: UTF-8'
                ));
        }
        curl_setopt($curl, CURLOPT_HTTPHEADER, $this->headers);

        if ($this->debug){
            echo '<h1>Options</h1>';
            var_dump($this->options);
            echo '<h1>Header</h1>';
            var_dump($this->headers);
        }

        // set options
        foreach($this->options as $name => $val) {
            if (is_string($name)) {
                $name = constant(strtoupper($name));
            }
            curl_setopt($curl, $name, $val);
        }
        return $curl;
    }
    /**
     * Download multiple files in parallel
     *
     * Calls {@link multi()} with specific download headers
     *
     * <code>
     * $c = new curl;
     * $c->download(array(
     *              array('url'=>'http://localhost/', 'file'=>fopen('a', 'wb')),
     *              array('url'=>'http://localhost/20/', 'file'=>fopen('b', 'wb'))
     *              ));
     * </code>
     *
     * @param array $requests An array of files to request
     * @param array $options An array of options to set
     * @return array An array of results
     */
    public function download($requests, $options = array()) {
        $options['CURLOPT_BINARYTRANSFER'] = 1;
        $options['RETURNTRANSFER'] = false;
        return $this->multi($requests, $options);
    }
    /*
     * Mulit HTTP Requests
     * This function could run multi-requests in parallel.
     *
     * @param array $requests An array of files to request
     * @param array $options An array of options to set
     * @return array An array of results
     */
    protected function multi($requests, $options = array()) {
        $count   = count($requests);
        $handles = array();
        $results = array();
        $main    = curl_multi_init();
        for ($i = 0; $i < $count; $i++) {
            $url = $requests[$i];
            foreach($url as $n=>$v){
                $options[$n] = $url[$n];
            }
            $handles[$i] = curl_init($url['url']);
            // Clean up
            $this->reset_options();
            $this->apply_options($handles[$i], $options);
            curl_multi_add_handle($main, $handles[$i]);
        }
        $running = 0;
        do {
            curl_multi_exec($main, $running);
        } while($running > 0);
        for ($i = 0; $i < $count; $i++) {
            if (!empty($options['CURLOPT_RETURNTRANSFER'])) {
                $results[] = true;
            } else {
                $results[] = curl_multi_getcontent($handles[$i]);
            }
            curl_multi_remove_handle($main, $handles[$i]);
        }
        curl_multi_close($main);
        return $results;
    }
    /**
     * Single HTTP Request
     *
     * @param string $url The URL to request
     * @param array $options
     * @return bool
     */
    protected function request($url, $options = array()) {
        // create curl instance
        $curl = curl_init($url);
        $options['url'] = $url;
        // Clean up
        $this->reset_options();
        $this->apply_options($curl, $options);
        if ($this->cache && $ret = $this->cache->get($this->options)) {
            return $ret;
        } else {
            $ret = curl_exec($curl);
            if ($this->cache) {
                $this->cache->set($this->options, $ret);
            }
        }

        $this->info  = curl_getinfo($curl);
        $this->error = curl_error($curl);

        if ($this->debug){
            echo '<h1>Return Data</h1>';
            var_dump($ret);
            echo '<h1>Info</h1>';
            var_dump($this->info);
            echo '<h1>Error</h1>';
            var_dump($this->error);
        }

        curl_close($curl);

        if (empty($this->error)) {
            return $ret;
        } else {
            throw new Exception($this->error);
        }
    }

    /**
     * HTTP HEAD method
     *
     * @see request()
     *
     * @param string $url
     * @param array $options
     * @return bool
     */
    public function head($url, $options = array()) {
        $options['CURLOPT_HTTPGET'] = 0;
        $options['CURLOPT_HEADER']  = 1;
        $options['CURLOPT_NOBODY']  = 1;
        return $this->request($url, $options);
    }

    /**
     * Recursive function formating an array in POST parameter
     *
     * @param array $arraydata - the array that we are going to format and add into &$data array
     * @param string $currentdata - a row of the final postdata array at instant T
     *                when finish, it's assign to $data under this format: name[keyname][][]...[]='value'
     * @param array $data - the final data array containing all POST parameters : 1 row = 1 parameter
     */
    private function make_array_field($fieldname, $arraydata, &$postfields) {
        global $depth;
        $depth++;
        foreach ($arraydata as $key=>$v) {
            $key = urlencode($key);
            if (is_object($v)) {
                $v = (array)$v;
            }
            if (is_array($v)) { //the value is an array, call the function recursively
                $newfieldname = $fieldname."[$key]";
                $this->make_array_field($newfieldname, $v, $postfields);
            } else {
                $postfields[] = $fieldname."[$key]=".urlencode($v);
            }
        }
    }

    /**
     * Transform a PHP array into POST parameter
     *
     * @param array $postdata
     * @return array containing all POST parameters  (1 row = 1 POST parameter)
     */
    private function make_postfields($postdata) {
        global $depth;
        $depth = 0;
        if (is_object($postdata)) {
            $postdata = (array)$postdata;
        }
        $postfields = array();
        foreach ($postdata as $key=>$value) {
            $key = urlencode($key);
            if (is_object($value)) {
                $value = (array)$value;
            }
            if (is_array($value)) {
                $this->make_array_field($key, $value, $postfields);
            }  else {
                $postfields[] = $key.'='.urlencode($value);
            }
        }
        $convertedpostdata = implode('&', $postfields);
        return $convertedpostdata;
    }

    /**
     * HTTP POST method
     *
     * @param string $url
     * @param array|string $params
     * @param array $options
     * @return bool
     */
    public function post($url, $params = '', $options = array()) {
        $options['CURLOPT_POST'] = 1;
        if (is_array($params)) {
            $params = $this->make_postfields($params);
        }
        $options['CURLOPT_POSTFIELDS'] = $params;
        return $this->request($url, $options);
    }

    /**
     * HTTP GET method
     *
     * @param string $url
     * @param array $params
     * @param array $options
     * @return bool
     */
    public function get($url, $params = array(), $options = array()) {
        $options['CURLOPT_HTTPGET'] = 1;

        if (!empty($params)){
            $url .= (stripos($url, '?') !== false) ? '&' : '?';
            $url .= http_build_query($params, '', '&');
        }
        return $this->request($url, $options);
    }

    /**
     * HTTP PUT method
     *
     * @param string $url
     * @param array $params
     * @param array $options
     * @return bool
     */
    public function put($url, $params = array(), $options = array()) {
        $file = $params['file'];
        if (!is_file($file)){
            return null;
        }
        $fp   = fopen($file, 'r');
        $size = filesize($file);
        $options['CURLOPT_PUT']        = 1;
        $options['CURLOPT_INFILESIZE'] = $size;
        $options['CURLOPT_INFILE']     = $fp;
        if (!isset($this->options['CURLOPT_USERPWD'])){
            $this->set_options(array('CURLOPT_USERPWD'=>'anonymous: noreply@moodle.org'));
        }
        $ret = $this->request($url, $options);
        fclose($fp);
        return $ret;
    }

    /**
     * HTTP DELETE method
     *
     * @param string $url
     * @param array $params
     * @param array $options
     * @return bool
     */
    public function delete($url, $param = array(), $options = array()) {
        $options['CURLOPT_CUSTOMREQUEST'] = 'DELETE';
        if (!isset($options['CURLOPT_USERPWD'])) {
            $options['CURLOPT_USERPWD'] = 'anonymous: noreply@moodle.org';
        }
        $ret = $this->request($url, $options);
        return $ret;
    }
    /**
     * HTTP TRACE method
     *
     * @param string $url
     * @param array $options
     * @return bool
     */
    public function trace($url, $options = array()) {
        $options['CURLOPT_CUSTOMREQUEST'] = 'TRACE';
        $ret = $this->request($url, $options);
        return $ret;
    }
    /**
     * HTTP OPTIONS method
     *
     * @param string $url
     * @param array $options
     * @return bool
     */
    public function options($url, $options = array()) {
        $options['CURLOPT_CUSTOMREQUEST'] = 'OPTIONS';
        $ret = $this->request($url, $options);
        return $ret;
    }
    public function get_info() {
        return $this->info;
    }
}

/**
 * This class is used by cURL class, use case:
 *
 * $c = new curl(array('cache'=>true), 'module_cache'=>'repository');
 * $ret = $c->get('http://www.google.com');
 *
 * @copyright  Dongsheng Cai {@see http://dongsheng.org}
 * @license    http://www.gnu.org/copyleft/gpl.html GNU Public License
 */
class curl_cache {
    /** @var string */
    public $dir = '';
    /**
     *
     * @param string @module which module is using curl_cache
     *
     */
    function __construct() {
        $this->dir = '/tmp/';
        if (!file_exists($this->dir)) {
            mkdir($this->dir, 0700, true);
        }
        $this->ttl = 1200;
    }

    /**
     * Get cached value
     *
     * @param mixed $param
     * @return bool|string
     */
    public function get($param) {
        $this->cleanup($this->ttl);
        $filename = 'u_'.md5(serialize($param));
        if(file_exists($this->dir.$filename)) {
            $lasttime = filemtime($this->dir.$filename);
            if(time()-$lasttime > $this->ttl)
            {
                return false;
            } else {
                $fp = fopen($this->dir.$filename, 'r');
                $size = filesize($this->dir.$filename);
                $content = fread($fp, $size);
                return unserialize($content);
            }
        }
        return false;
    }

    /**
     * Set cache value
     *
     * @param mixed $param
     * @param mixed $val
     */
    public function set($param, $val) {
        $filename = 'u_'.md5(serialize($param));
        $fp = fopen($this->dir.$filename, 'w');
        fwrite($fp, serialize($val));
        fclose($fp);
    }

    /**
     * Remove cache files
     *
     * @param int $expire The number os seconds before expiry
     */
    public function cleanup($expire) {
        if($dir = opendir($this->dir)){
            while (false !== ($file = readdir($dir))) {
                if(!is_dir($file) && $file != '.' && $file != '..') {
                    $lasttime = @filemtime($this->dir.$file);
                    if(time() - $lasttime > $expire){
                        @unlink($this->dir.$file);
                    }
                }
            }
        }
    }
    /**
     * delete current user's cache file
     *
     */
    public function refresh() {
        if($dir = opendir($this->dir)){
            while (false !== ($file = readdir($dir))) {
                if(!is_dir($file) && $file != '.' && $file != '..') {
                    if(strpos($file, 'u_')!==false){
                        @unlink($this->dir.$file);
                    }
                }
            }
        }
    }
}
?>
