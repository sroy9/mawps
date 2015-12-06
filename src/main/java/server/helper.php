<?php
$GLOBALS['THRIFT_ROOT'] = '../curator/demo/thrift';
// require_once $GLOBALS['THRIFT_ROOT'].'/Thrift.php';
// require_once $GLOBALS['THRIFT_ROOT'].'/protocol/TBinaryProtocol.php';
// require_once $GLOBALS['THRIFT_ROOT'].'/transport/TSocket.php';
// require_once $GLOBALS['THRIFT_ROOT'].'/transport/THttpClient.php';
// require_once $GLOBALS['THRIFT_ROOT'].'/transport/TBufferedTransport.php';
// require_once $GLOBALS['THRIFT_ROOT'].'/transport/TFramedTransport.php';
// require_once $GLOBALS['THRIFT_ROOT'].'/packages/curator/Curator.php';

// These my_ functions work out if php has multibyte support and uses
// the mb version if available.
// Note str_replace is multi-byte safe so we don't need a my_ version.

mb_internal_encoding("UTF-8");

function my_substr($s, $start, $length="-1") {
    if ($length == "-1") {
        $length = my_strlen($s) - $start;
    }

    if (function_exists('mb_substr')) {
        return mb_substr($s, $start, $length);
    } else {
        return substr($s, $start, $length);
    }
}

function my_strlen($s) {
    if (function_exists('mb_strlen')) {
        return mb_strlen($s);
    } else {
        return strlen($s);
    }
}

function my_reg_replace($pattern, $replacement, $string) {
    if (function_exists('mb_ereg_replace')) {
        return mb_ereg_replace($pattern, $replacement, $string);
    } else {
        return preg_replace($pattern, $replacment, $string);
    }
}

function sanitize_text($text) {
    $max_size = 10000;
    if (strlen($text) > $max_size)
    {
        $text = substr($text, 0, $max_size);
    }
    $text = strip_tags(str_replace(array("\r\n", "\r", "\n"), "\n", stripslashes($text)));
    
    $text = checkPunct($text); //only this is new -JB 7/7/14
    return $text;
}

//returns an error sentence if the ratio of punctuation characters to alphanumeric characters is >4
function checkPunct($text){
    $numAlpha = 0;
    preg_match_all("/\w/", $text, $numAlpha);
    $numPunct = 0;
    preg_match_all("/\p{P}\p{S}/", $text, $numPunct);
    if ( (count($numAlpha[0])+1)/(count($numPunct[0])+1) < 4.0 ){
        $text = "Too much punctuation, please try again.";
    }   
    return $text; 
}


function nls2p($str) {
  return str_replace('<p></p>', '', '<p>'
        . my_preg_replace('#([\r\n]\s*?[\r\n]){2,}#', '</p>$0<p>', $str)
        . '</p>');
}

// Check if the label is going to cause problems when used inside a html attribute
function is_bad_label($label) {
    return $label == "#" or $label == '$' or $label == "''" or $label == "``" or $label == "'" or $label == "`" or $label == "," or $label == "." or $label ==":";
}

//builds an array from labels
function build_array($labels) {
    $result = array();
    foreach ($labels as $i => $span) {
        if (!isset($result[$span->start])) {
            $result[$span->start] = array();
        }
        $result[$span->start][$span->ending] = $span;
    }
    return $result;
}


function getLabelingHTML($text, $labeling, $name, $newline = false, $offset = 0) {
    if (is_null($labeling)) { return ""; }
    $labels = $labeling->labels;
    $result = '';
    foreach ($labels as $i => $span) {
        $pos = $i+$offset;
        $result .= getSpanHTML($text, $span, $name.$pos);
        if ($newline) $result .= "<br/>";
    }
    return $result;
}

function getSpanHTML($text, $span, $name) {
    $result = "";
    $result .= '<span class="span" id="' . $name. '">';
    if (!is_null($span->label)) {
        if (is_bad_label($span->label)) {
            $result .= '<span class="label PUNC">';
        } else {
            $result .= '<span class="label '.$span->label.'">';
        }
        $result .= htmlspecialchars($span->label);
        $result .= "</span>";
        $result .= ' ';
    }
    if (is_bad_label($span->label)) {
        $result .= '<span class="token PUNC">';
    } else {
        $result .= '<span class="token '.$span->label.'">';
    }
    $result .= htmlspecialchars(my_substr($text, $span->start, $span->ending - $span->start));
    $result .= '</span>';
    if (!is_null($span->attributes)) {
        foreach ($span->attributes as $key => $value) {
            $result .= ' <span class="attribute ' .$span->label.'">'; 
            $result .= htmlspecialchars("[".$key . ": " . $value."]");
            $result .= "</span>&nbsp;";
        }
    }
    $result .= '</span>&nbsp;';
    return $result;
}


function getLabelingJavascript($labeling, $name, $offset=0) {
    if (is_null($labeling)) { return ""; }
    $labels = $labeling->labels;
    $result = "";
    foreach ($labels as $i => $span) {
        $result .= getSpanJavascript($span, $name, $i+$offset);
    }
    return $result;
}

function getSpanJavascript($span, $name, $i) {
    if (is_null($span)) { return  ""; }
    $result = '$("#' . $name . $i . '").click( function() {'. "\n";
    $result .= '$("#text").removeHighlight();' . "\n";
    $result .= '$("#text").highlight(' . "$span->start, $span->ending, 'highlight');\n";
    $result .= "});\n";
    return $result;
}


function getForestHTML($text, $forest, $name, $spans=false) {
    if (is_null($forest)) { return ""; }
    $result = "";
    $counter = 0;

    foreach ($forest->trees as $i => $tree) {
        $topnode = $tree->nodes[$tree->top];
        if (isset($tree->score)) {
            $result .= 'Score: ' .$tree->score .'<br/>';
        }
        $result .= getNodeHTML($text, $topnode, $tree->nodes, 0, $name, $counter, "", true, $spans);
        $result .= "<br/>";
    }
    return $result;
}

function getForestJavascript($forest, $name) {
    if (is_null($forest)) { return ""; }
    $counter = 0;
    $result = "";
    foreach ($forest->trees as $i => $tree) {
        $topnode = $tree->nodes[$tree->top];
        $result .= getNodeJavascript($topnode, $tree->nodes, $name, $counter);
    }
    return $result;

}

function getNodeHTML($text, $node, $nodes, $padding, $name, &$counter, $label="", $first=true, $spans=false) {
    $result = "";
    $dpadding = 0;
    $counter += 1;
    if (!$first) {
        for ($i = 0; $i < $padding; $i++) {
            $result .= "&nbsp;";
        }
    }
    if (!($label == "")) {
        $result .= "<span class='edge'>&lt;$label&gt;</span>&nbsp;";
        $dpadding += my_strlen($label) + 2;
    }
    $span = $node->span;
    if (!($node->label == "dependency node")) {
        $result .= '<span class="label" id="' .$name . $counter. '">'.$node->label.'</span>&nbsp;&nbsp;';
        $dpadding += my_strlen($node->label) + 2;
    }

    if ($spans) {
        $result .= getSpanHTML($text, $span, $name.$counter);
        $result .= '<br/>';
        //$dpadding += $span->ending-$span->start+1;
        $dpadding++;
    }
    if (!is_null($node->children) && !empty($node->children)) {
        ksort($node->children);
        $padding += $dpadding;
        $first = true;
        if ($spans) {
            $first = false;
        }
        foreach ($node->children as $index => $nlabel) {
            $result .= getNodeHTML($text, $nodes[$index], $nodes, $padding, $name, $counter, $nlabel, $first, $spans);
            $first = false;
            
        }
    } else if (!$spans) {
        $result .= getSpanHTML($text, $span, "");
        $result .= '<br/>';
    }
    return $result;
}

function getNodeJavascript($node, $nodes, $name, &$counter) {
    $counter++;
    $result = "";
    $result .= getSpanJavascript($node->span, $name, $counter);
    if (!is_null($node->children) && !empty($node->children)) {
        ksort($node->children);
        foreach ($node->children as $index => $nlabel) {
            $result .= getNodeJavascript($nodes[$index], $nodes, $name, $counter);
        }
    }
    return $result;
}

function wikify2($record, $wikid){
    // build the wiki entry array first.
    $wiki_entries = array();
    $wiki_entries_end = array();
    foreach($wikid->labels as $id => $span) {
        if((array_key_exists($span->start, $wiki_entries) && $span->ending > $wiki_entries[$span->start]["end"]) || !array_key_exists($span->start, $wiki_entries)) {
            $wiki_entries[$span->start] = array('label' => $span->label, 'is_linked' => $span->attributes["IsLinked"], 'cat_attribs' => $span->attributes["TitleWikiCatAttribs"], 'end' => $span->ending, 'text' => my_substr($record->rawText, $span->start, $span->ending - $span->start));
            if(array_key_exists($span->ending, $wiki_entries_end) && $span->start < $wiki_entries_end[$span->ending]) {
                // replace the wiki entry starting at a later point, but ending at the same point with this one
                unset($wiki_entries[$wiki_entries_end[$span->ending]]);
                $wiki_entries_end[$span->ending] = $span->start;
            } else if(!array_key_exists($span->ending, $wiki_entries_end)) {
                $wiki_entries_end[$span->ending] = $span->start;
            } else {
                unset($wiki_entries[$span->start]);
            }
        }
    }

    // now we have a wiki entry array indexed by span start position. let's print out the all of the data! (with links!)
    ksort($wiki_entries);
    //echo "<pre>";
    //var_dump($wiki_entries);
    //echo "</pre>";

$result = mb_convert_encoding($record->rawText, 'UTF-8');
    $offset = 0;
    $lastStart = 0;
    $lastEnd = 0;
    foreach($wiki_entries as $start_pos => $entry) {
        //If we have an entry that is completely within the last entry, skip it. Prevents link within a link problems
        if($start_pos > $lastStart && $entry["end"] < $lastEnd)continue;
        $lastStart = $start_pos;
        $lastEnd = $entry["end"];
        $start_pos += $offset;
        $entry["end"] += $offset;
        if($entry["is_linked"] == "true") { //$entry["label"] != "UNMAPPED") {
            $result = my_substr($result, 0, $start_pos)."<a class=\"wiki\" href=\"".$entry["label"]."\" cat=\"".$entry["cat_attribs"]."\">".my_substr($result, $start_pos, $entry["end"] - $start_pos)."</a>".my_substr($result, $entry["end"]);
            $offset += my_strlen("<a class=\"wiki\" href=\"\" cat=\"\"></a>"); // extra text when we link
            $offset += my_strlen($entry["label"]);
            $offset += my_strlen($entry["cat_attribs"]);
        } else if($entry["label"] == "UNMAPPED") {
            $result = my_substr($result, 0, $start_pos)."<b>".my_substr($result, $start_pos, $entry["end"] - $start_pos)."</b>".my_substr($result, $entry["end"]);
            $offset += my_strlen("<b></b>");
        }
    }
    //return "<pre>".print_r($wiki_entries, true)."</pre>";
    return nl2br($result);
}

//this is used in the demos
function getHTMLForLabels($record, $labeling,$demoName=null) {
    if($demoName == "wikifier") {
	   return wikify2($record,$labeling);
    }
    $labels = build_array($labeling->labels);
    $rawtext = $record->rawText;
    $sents = $record->labelViews["sentences"]->labels;
    ksort($labels);
    $previous = 0;

    $j = 0;
    $result = '<div class="output"><p><span class="sentence">';
    foreach ($labels as $start => $ends) {
        if ($start > $sents[$j]->ending) {
            if ($previous < $sents[$j]->ending) {
                $result .= htmlspecialchars(my_substr($rawtext, $previous, $sents[$j]->ending - $previous));
                $previous = $sents[$j]->ending;
            }
            $result .= '</span><span class="sentence">';
            $j = $j + 1;
        }
        ksort($ends);
        if ($start - $previous > 0) {
            $result .= htmlspecialchars(my_substr($rawtext, $previous, $start - $previous));
        }
        foreach ($ends as $end => $span) {
            $result .= getSpanHTML($rawtext, $span, "");
            $previous = $end;
        }

    }
    $result .= htmlspecialchars(my_substr($rawtext, $previous));
    $result .= '</span></p></div>';
    return nl2br($result);
}




function predict_roles($verb) {
	$h = popen("../demo_functions/rolepredict.sh ".$verb, "r");
/* 	$h = popen("../bin/role-predict.sh ".$verb, "r"); */
	$roles = array();
	while(!feof($h)) {
		$line = fgets($h);
		preg_match("/(\S+)\s+(.+)/", $line, $m);
		if(count($m) == 3) {
			$roles[$m[1]] = $m[2];
		}
	}
	pclose($h);
/* 	print_r($roles); */
	return $roles;
}

?>