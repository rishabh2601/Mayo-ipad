// YOU HAVE TO INCLUDE prinit.js BEFORE page.js

function getPageId() {
    var sPath = window.location.pathname;
    var lastSlash = sPath.lastIndexOf("/");
    return sPath.substring(lastSlash+1, sPath.indexOf(".html"));

    // try 3, put it in the session
    //return getStorage().getItem('currentPageId');
    //return queryParam('targetPageId');
}

function clearAnswers() {
    for (var i = 0; i < qorderarr.length; i++) {
        console.log('Cleared ' + qorderarr[i]+ansSuffix);
        getStorage().removeItem(qorderarr[i]+ansSuffix);
    }
}

function echoSessionStorage(l) {
    console.log("Echoing session storage from " + l);
    for (var i = 0; i < getStorage().length; i++) {
      var key = getStorage().key(i);
      var value = getStorage().getItem(key);
      console.log("SS: " + key + " = " + value);
    }
}

function initPainReport() {
    if (typeof getStorage() == "undefined") {
        console.log("*********NO getStorage()");
    }
    // clear any session storage answers
    clearAnswers();

    // Took out all the old stuff trying to init PIN and Server
    // from properties or query params, doing it via javascript
    // injection now. --KG 11/3/13
}

function echoAnswers() {
    for (var i = 0; i < qorderarr.length; i++) {
        console.log(qorderarr[i] + ' = ' + getStorage().getItem(qorderarr[i]+ansSuffix));
    }
}

function prepAnswersForSubmit() {
	var finalAns = {};
	for (var i = 0; i < qorderarr.length; i++) {
		finalAns[qorderarr[i]] = getStorage().getItem(qorderarr[i]+ansSuffix);
	}
	return JSON.stringify(finalAns);
}

function ajaxPostAnswers(url, params) {
       // console.log("BLAH "+document.documentElement.innerHTML);
    var pinElement = document.getElementById('PINcheck');
    pinElement.innerHTML += '<br/><div id="ServerResponse">Waiting for server...</div><br/>';

    var url = getStorage().getItem("SERVER");
    var params = getStorage().getItem("AJAXANS");
    var xmlhttp;
    if (window.XMLHttpRequest) {
        // code for IE7+, Firefox, Chrome, Opera, Safari
        xmlhttp=new XMLHttpRequest();
    }
    xmlhttp.open("POST", url, true);

    //Send the proper header information along with the request
    xmlhttp.setRequestHeader("Content-type", "application/x-www-form-urlencoded");
    xmlhttp.setRequestHeader("Content-length", params.length);
    xmlhttp.setRequestHeader("Connection", "close");

    xmlhttp.onreadystatechange = function() {
        //Call a function when the state changes.
        var respElement = document.getElementById('ServerResponse');
        var responseMsg = '';
        if(xmlhttp.readyState == 4 && xmlhttp.status == 200) {
            responseMsg += xmlhttp.responseText;
        }
        else {
            responseMsg += "Invalid response from server, please re-submit";
        }
        responseMsg += "<p><input type=\"button\" value=\"Close\" onclick=\"__blhelper.finishMe()\"/></p>";
        respElement.innerHTML = responseMsg;
    }
    xmlhttp.send(params);
}

function checkPIN(el) {
    var rval = false;
    //console.log("Password is " + el.value);
    if (varCheck(el) && el.type == 'password' && el.value.length == 4) {
        var storedPIN = getStorage().getItem('PIN');
        //console.log("Stored PIN from Java " + storedPIN);

        var respElement = document.getElementById('PINcheck');
        if (varCheck(storedPIN) && el.value == storedPIN) {
            var resultHTML = 'Correct PIN entered<br/>';
            resultHTML += '<input id="ajaxbutton" type="button" value="Submit" onclick="ajaxPostAnswers()"/>';
            //console.log(resultHTML);
            respElement.innerHTML=resultHTML;
            // <input id="sbutton" name="sbutton" type="submit" value="submit"/>';

        } else {
            respElement.innerHTML='Incorrect PIN entered, try again';
        }
    }
    return rval;
}

// No longer doing form-based submission, now doing AJAX
function chkAnswers() {
    var prresults = document.getElementById("_prans");
    if (!varCheck(prresults)) {
        console.log("Could not get DOM element _prans");
        return;
    }
    var resultHTML = '';
    var ajaxAns = '';
    for (var i = 0; i < qorderarr.length; i++) {
        var answer = getStorage().getItem(qorderarr[i]+ansSuffix);
         if (answer == '{}' || answer == null || typeof answer == 'undefined') {
            resultHTML += '<li>Please answer the question on the ' + '<a href="#" onclick=\'navToQuestionURL(' + i + ');\' >' + qorderarr[i] + '<\a> page </li>';
        }
    }
    if (resultHTML == '') {
        var submitURL = getStorage().getItem("SERVER");
        var configPIN = getStorage().getItem("PIN");
        ajaxAns += "PIN="+configPIN;

        // All questions were answered, instead ask for the PIN
        //resultHTML += 'Thank you for completing the questions. Please enter your PIN and press submit<br/>\n';
        //resultHTML += '<p><form method="POST" action="'+submitURL+'" >\n';
        for(var i = 0; i < qorderarr.length; i++) {
                // bizarre, but the double quotes and single quotes have to be this way or the HTML is scrogged!!!
        		//resultHTML += "<input type='hidden' name='"+qorderarr[i]+"Ans' value='"+getStorage().getItem(qorderarr[i]+ansSuffix)+"'/>";
                ajaxAns += "&"+qorderarr[i]+"Ans="+getStorage().getItem(qorderarr[i]+ansSuffix);
        }
        resultHTML += '<input type="password" size="4" name="PIN" maxLength="4" onkeyup="checkPIN(this)"/>';
        resultHTML += '<br/><div id="PINcheck"></div>';
        //resultHTML += '</form></p>';
        // AJAX button
        getStorage().setItem("AJAXANS", ajaxAns);


        //console.log(resultHTML);
        prresults.innerHTML = resultHTML;
    } else {  // this completes the case where there were unanswered questions
        prresults.innerHTML = '<ul>'+resultHTML+'</ul>';
    }
}


function saveAnswer(ans) {
    var qId = getPageId();
    console.log("Saving for qId " + qId + ": Answer = " + ans);
    getStorage().setItem(qId + ansSuffix, ans);
}

function getDisplayQuestionURL(delta) {
    // get the current question
    var qId = getPageId();
    var qn = qorderarr.indexOf(qId);
    var targetQn = qn + Number(delta);
    var targetURL = '';
    
    if (targetQn < 0) {
        targetURL = qorderarr[0]+'.html';
    } else if (targetQn >= qorderarr.length) {
        targetURL = 'submit.html';
    } else {  // must be a valid index
        targetURL = qorderarr[targetQn]+'.html';
    }
    return targetURL;
}


function navToQuestionURL(index) {
    var qn = index;
    var targetQn = qn;
    var targetURL = '';
    
    if (targetQn < 0) {
        targetURL = qorderarr[0]+'.html';
    } else if (targetQn >= qorderarr.length) {
        targetURL = 'submit.html';
    } else {  // must be a valid index
        targetURL = qorderarr[targetQn]+'.html';
    }
    window.location.assign(targetURL);
}

function displayTarget(delta) {
    var targetURL = getDisplayQuestionURL(delta);
    var targetPageId = getPageId();
    console.log("In displayTarget, delta is " + delta + ", targetPageId is " + targetPageId + ", targetURL is " + targetURL);
    if (targetPageId != null && targetPageId != 'undefined') { 
        window.location.assign(targetURL);
        // now we set the page id on the page load (finalizePageRender below)
    } else {
        console.log('ERROR: could not get target page id!');
    }
}

function printNav(/* enableFlag */) {
    var prNavEl = document.getElementById('_prnav');
    var qId = getPageId();
    var qn = qorderarr.indexOf(qId);
    console.log("Next question: " + getDisplayQuestionURL(1));

    if (varCheck(prNavEl)) {
        /* if (enableFlag == false) {
            prNavEl.innerHTML = '<div id="_prnav"></div>';
        } else */ if (qn == 0) {
            prNavEl.innerHTML = '<input type="button" id="nextBtn" value="Next" onclick="displayTarget(1)" />';

            //prNavEl.innerHTML = '<div id="_prnav"><a href="./'+getDisplayQuestionURL(1)+'">NEXT</a></div>';
        } else {
            prNavEl.innerHTML = '<input type="button" value="Prev" onclick="displayTarget(-1)"><input type="button" id="nextBtn" value="Next" onclick="displayTarget(1)" />';

            //prNavEl.innerHTML = '<div id="_prnav"><a href="'+getDisplayQuestionURL(-1)+'">PREV</a>   <a href="'+getDisplayQuestionURL(1)+'">NEXT</a></div>';
        }
    }

}

function disableNextButton(disableIt) {
    var btnEl = document.getElementById("nextBtn");
    if (varCheck(btnEl)) {
        console.log("FOUND THE BUTTON " + disableIt);
        btnEl.disabled=disableIt;
    }
}

/*
 * YYY Attempt 2. When we leave a page (anyway it happens), we want to
 * save the answer state of that page for all answer elements on it. The
 * issue is how to determine all of those answer elements. Solution idea
 * is to create a custom attribute _prAnswer with it's value set to a
 * unique question identifier. When we save we read the property of the
 * widget and store { PK : VALUE }.
 */
function collectPageAnswerState() {
    var ansArr = getAllElementsWithAttribute('_prAnswer');
    var newArr = {};
    //console.log('Num elements with _prAnswer: ' + ansArr.length);
    
    for (var i = 0; i < ansArr.length; i++)  {
        // all array entries are widget elements
        // Can't use a switch because the element we are dealing with is
        // identified either by type attribute or tagname
        var thisElem = ansArr[i];
        var tagname  = thisElem.nodeName.toLowerCase();
        var tagtype  = thisElem.getAttribute('type');
        if (varCheck(tagtype)) {
            tagtype = tagtype.toLowerCase();
        }
        var itemId   = thisElem.getAttribute('_prAnswer');
        
        //console.log('\tElement ' + i + ' ' + tagname + ' ' + tagtype + ' ' + itemId);
        //alert('\tElement ' + i + ' ' + tagname + ' ' + tagtype + ' ' + itemId);
        if (tagname == 'range' || (tagtype != null && tagtype == 'range'))  {
            // slider: if we have a range we always record it's value
            newArr[itemId] = thisElem.value;
        }
        else if (tagname == 'input' && (tagtype != null && (tagtype == 'radio' || tagtype == 'checkbox'))) {
            // check to see if it is checked
             if (thisElem.checked == true) {
                newArr[itemId] = true;
             }
        } else if (tagname == 'textarea' || (tagtype != null && tagtype == 'text')) {
            var txtValue = thisElem.value;
            if (txtValue != null && txtValue != 'undefined' && txtValue.trim().length > 0) {
                newArr[itemId] = txtValue;
            }
        } else if (tagname == 'select') {  // not handling datalist right now
            newArr[itemId] = thisElem.options[thisElem.selectedIndex].value;
        } else if (tagname == 'area') {  // added to handle area
        		key = $('#myimg').mapster('get');
        		// A clickable area map will have many elements with tagname area, but 
        		// the _prAnswer attribute indicates the current tag's region while the
        		// key represents the selected value. If they are the same, save it as true
        		if (itemId == key) {
        			newArr[itemId] = true;
        		} 
        }
    }
    //console.log(JSON.stringify(newArr));
    return newArr;
}

function onUnloadListener() {
    var pageArr = collectPageAnswerState();
    console.log('UNLOAD CALLED');
    if (varCheck(pageArr)) {
        console.log('Saving page state: ' + JSON.stringify(pageArr));
        saveAnswer(JSON.stringify(pageArr));
    }
}

// check to see if there was a label. Thanks to
// http://stackoverflow.com/questions/285522/find-html-label-associated-with-a-given-input

function findLabelForElement(el) {
    if (varCheck(el)) {
        var idVal = el.id;
        console.log("In findLabelForElement, element id is " + idVal);
        labels = document.getElementsByTagName('label');
        for (var i = 0; i < labels.length; i++) {
            console.log("   checking label " + labels[i].htmlFor);
            if (labels[i].htmlFor == idVal)
                return labels[i];
        }
    }
}


function onChangeListener() {
	
    var pageArr = collectPageAnswerState();
    console.log("ANSWERS: " + JSON.stringify(pageArr));
	
    if (varCheck(pageArr) && JSON.stringify(pageArr).length > 2) {  // 2 for {}
        disableNextButton(false);
        //printNav(true);
    } else {
        disableNextButton(true);
        //printNav(false);
    }
    
    // change the label if there is one
        /*
    var label = findLabelForElement(el);
    if (varCheck(label)) {
        try {
            label.innerHTML = el.value;
        }
        catch (err) {
            console.log("element was null in OnChangeListener");
        }
    }
        */
}

/*
 * This should be called after the rest of the page has loaded. The goal is
 * to 1) check for answers on this page, 2) if there are not answers, disable the
 * Next button, 3) if there are answers to populate state in the answer options.
 */ 
function finalizePageRender() {
    echoSessionStorage('finalizePageRender');

    // standard content template substitutions
    // Must be done before trying to manipulate widget properties
    printNav(/* true */);
    printFooterIndex();
    printQuestion();

    var pageId = getPageId();
    var pageAns = getStorage().getItem(pageId+ansSuffix);
    console.log("Saving to PageAns " + pageAns);
    if (varCheck(pageAns) && pageAns.length > 2) { // JSON {}
        disableNextButton(false);
        //printNav(/* true */);
    } else {
        disableNextButton(true);
        //printNav(/* false */);
        return;
    }
       
    // OK, this is the part where we re-render answers    
    var ansArr = getAllElementsWithAttribute('_prAnswer');
    var arrayAns = JSON.parse(pageAns);

    //console.log('RENDER: Num elements with _prAnswer: ' + ansArr.length);

    // for each tag found, determine if we have an answer already for it
    for (var i = 0; i < ansArr.length; i++) {
        // all array entries are widget elements
        // Can't use a switch because the element we are dealing with is
        // identified either by type attribute or tagname
        var thisElem = ansArr[i];
        var tagname = thisElem.nodeName.toLowerCase();
        var tagtype = thisElem.getAttribute('type');
        if (varCheck(tagtype)) {
            tagtype = tagtype.toLowerCase();
        }
        var itemId = thisElem.getAttribute('_prAnswer');
        var ansValue = arrayAns[itemId];

        //console.log('\tElement ' + i + ' ' + tagname + ' ' + tagtype + ' ' + itemId + ' VALUE = ' + ansValue);
        //console.log("thisElem is " + thisElem);
        if (varCheck(ansValue)) {
            if (tagname == 'range' || (tagtype != null && tagtype == 'range')) {
                // slider: if we have a range we always record it's value
                thisElem.value = ansValue;
                // check for a label associated with this slider
                var label = findLabelForElement(thisElem);
                if (varCheck(label)) {
                    label.innerHTML = ansValue;
                }
            } else if (tagname == 'input' && (tagtype != null && (tagtype == 'radio' || tagtype == 'checkbox'))) {
                // check to see if it is checked
                if (ansValue == true) {
                    //console.log("SETTING IT TO CHECKED");
                    thisElem.checked = true;                    
                }
            } else if (tagname == 'textarea' || (tagtype != null && tagtype == 'text')) {
                thisElem.value = ansValue;
            } else if (tagname == 'select') {// not handling datalist right now
                var optionNodes = thisElem.options;
                if (varCheck(optionNodes)) {
                    for (var n = 0; n < optionNodes.length; n++) {
                        var optionValue = optionNodes.item(n).value;
                        // now check that this is defined
                        if (varCheck(optionValue) && optionValue == ansValue) {
                            // select it
                            optionNodes.item(n).selected = true;
                            break;  // terminate the loop when found, single select
                            //thisElem.options[thisElem.options.selectedIndex].selected = true;
                        }
                    }
                }
                thisElem.options[thisElem.selectedIndex].value = ansValue;
            } else if (tagname == 'area' ) {
                // like above when collecting page state, there may be many area elements, so
                // we have to find the one that should be true and set it for mapster
                if (ansValue) {	
                    // note this assumes the HTML page includes jquery
                    $('#myimg').mapster('set',true,itemId);  // itemId should have the location, not ansValue
                }
     		}
       }
    }
}

function printFooterIndex() {
    var qId = getPageId();
    var qn = qorderarr.indexOf(qId);
    var prNavEl = document.getElementById('_prfooter');
    if (varCheck(prNavEl)) {
        prNavEl.innerHTML = 'Page ' + (qn+1) + ' of ' + qorderarr.length;
    }
}

function printQuestion() {
    var prNavEl = document.getElementById('_prquestion');
    var qId = getPageId();
    var qn = qorderarr.indexOf(qId);
    console.log("Printing question at location " + qn + " for page id " + qId);
    if (varCheck(prNavEl)) {
        prNavEl.innerHTML = qquesttxt[qn];
    }
}

/*
function checkPIN(el) {
var rval = false;
console.log("Password is " + el.value);
if (varCheck(el) && el.type == 'password' && el.value.length == 4) {
//var storedPIN = getStorage().getItem('PIN');
var storedPIN = window.JsPin.getPIN();
console.log("Stored PIN from Java " + storedPIN);
var respElement = document.getElementById('PINcheck');
if (varCheck(storedPIN) && el.value == storedPIN) {
respElement.innerHTML='Correct PIN entered<br/><input id="sbutton" type="submit" value="submit"/>';
} else {
respElement.innerHTML='Incorrect PIN entered, try again';
}
}
return rval;
}

function getProperties(f) {

console.log("READING " + f);
var reader = new FileReader();

alert("SET CALLBACK");
reader.onload = (function fileLoaded(theFile) {
return function(e) {
console.log("In read callback closure, event " + e);
var result = reader.result;
console.log("Read from file: " + result);
alert(result);
};
})();

alert("SET ONLOAD");
reader.onloadend = (function() {
console.log("READ TEXT FILE " + f);
});

reader.readAsText(f);
console.log("READ AS TEXT DONE");
console.log("EXIT getProperties");
}

*/