/**
* Parser variables
**/
var documentKeyGL = new Object();
var parsingFiltersGL = new Array();
var parsingFileInfoGL = new Object();
var parsingTemplateGL = {
	ScalingMultiplier__c: '100'
};

var killFileImportSwitch = false;


function isDocumentImported(){
    return (documentKeyGL && documentKeyGL.length > 0);
}

function getFileInfo() {
	return parsingFileInfoGL;
}


