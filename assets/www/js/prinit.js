// ===== These should be immutable global vars =====

const qorderarr=['DaysInPain','NightSleptPoorly','TroubleTakingCare','MissedSchoolWork','LeftEarly',
'UnableToEnjoy','FeelingSad','PainScore','TimePainLasts','PainFeeling','PainLocation',
'ActivityWhenPainBegin','NameAndDose','NonDrugTechniques','QuestionSleep','QuestionFeelings',
'QuestionSymptoms','Past7Sleeping','Past7Angry','Past7Schoolwork','Past7Attention',
'Past7Run','Past7Walk','Past7Fun','Past7Standing'];

const qquesttxt=new Array();
qquesttxt[0]='I want you to think about your pain in the last week: how many days have you had any pain?';
qquesttxt[1]='How many nights have you slept poorly (trouble falling asleep, waking up during sleep) because of pain?';
qquesttxt[2]='How many days have you had trouble taking care of yourself because of pain ?';
qquesttxt[3]='How many days have you missed school/work because of pain?';
qquesttxt[4]='How many days have you left school/work early because of pain?';
qquesttxt[5]='How many days have you been unable to do things you enjoy because of pain?';
qquesttxt[6]='How many days have you felt sad, mad or upset because of pain ?';
qquesttxt[7]='Please select a number, where 0 = "none" and 10 = "most intense", which best describes your pain';
qquesttxt[8]='How long did the pain last?';
qquesttxt[9]='How does the pain feel?';
qquesttxt[10]='Please select the body area in which you feel the most pain';
qquesttxt[11]='What were you doing when the pain began?';
qquesttxt[12]='Select the name and amount of medication you have taken';
qquesttxt[13]='What are the non-drug techniques you have tried?';
qquesttxt[14]='On average, how much sleep did you have during the night in the previous week?';
qquesttxt[15]='Select from the following list what best describes your thoughts/ feelings over the last week.';
qquesttxt[16]='Check if you experienced any of the following signs and/or symptoms over the last week.';
qquesttxt[17]='In the past 7 days I had trouble sleeping when I had pain';
qquesttxt[18]='In the past 7 days I felt angry when I had pain';
qquesttxt[19]='In the past 7 days I had trouble doing schoolwork when I had pain';
qquesttxt[20]='In the past 7 days it was hard for me to pay attention when I had pain';
qquesttxt[21]='In the past 7 days it was hard for me to run when I had pain';
qquesttxt[22]='In the past 7 days it was hard for me to walk one block when I had pain';
qquesttxt[23]='In the past 7 days it was hard to have fun when I had pain';
qquesttxt[24]='In the past 7 days it was hard to stay standing when I had pain';

const ansSuffix='Ans';

// ===== End global variables =====

function getStorage() {
    return sessionStorage;
}

function varCheck(v) {
    if (v == null || v == 'undefined') {
        return false;
    } else {
        return true;
    }
}

function queryParam(name) {
    name = name.replace(/[\[]/,"\\\[").replace(/[\]]/,"\\\]");
    var regexS = "[\\?&]"+name+"=([^&#]*)";
    var regex = new RegExp( regexS );
    var results = regex.exec( window.location.href );
    if( results == null )    return "";
    else    return results[1];
}

// Got this from http://stackoverflow.com/questions/9496427/how-to-get-elements-by-attribute-selector-w-native-javascript-w-o-queryselector
// Need this so we can find all widget elements with attribute "_prAnswer"
function getAllElementsWithAttribute(attribute) {
    var matchingElements = [];
    var allElements = document.getElementsByTagName('*');
    for (var i = 0; i < allElements.length; i++) {
        if (allElements[i].getAttribute(attribute)) {
            // Element exists with attribute. Add to array.
            matchingElements.push(allElements[i]);
        }
    }
    return matchingElements;
}
