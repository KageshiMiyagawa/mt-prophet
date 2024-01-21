window.onload = function() {
	// ダイアログ表示判定
	var dialogEvent = document.getElementById('dialogEvent');
	if (dialogEvent) {
		var dialogText = dialogEvent.getAttribute('dialogText');
		if (dialogText != null) {
			dispCommonDialog(dialogText);
		}
	}
	
	// フォーム初期表示
	var gameResultScrapingForm = document.getElementById("game-result-scraping-form");
	if (gameResultScrapingForm) {
		gameResultScrapingForm.style.display ="block";
	}
	
	var teamInfoScrapingForm = document.getElementById("team-info-scraping-form");
	if (teamInfoScrapingForm) {
		teamInfoScrapingForm.style.display ="block";
	}
	
	dispSelectedForm();
};

function dispSelectedForm() {
	var selectedFormField = document.getElementById("selectedFormField");
	if (selectedFormField) {
		var selectedFormType = selectedFormField.getAttribute("selectedFormType");
		dispForm(selectedFormType);
	}
}

function dispForm(formType) {
	dispElemName = "";
	elemNameArray = ["game-result-scraping-form", "team-info-scraping-form"];
	
	if (formType == "gameResult") {
		dispElemName = "game-result-scraping-form";
	} else if (formType == "teamInfo") {
		dispElemName = "team-info-scraping-form";
	} 
	dispElem = document.getElementById(dispElemName);
	dispElem.style.display = "block";
	// 表示対象以外のフォームは非表示
	for (var i = 0; i < elemNameArray.length; i++) {
		elemName = elemNameArray[i];
		if (dispElemName != elemName) {
			hideElem = document.getElementById(elemName);
			hideElem.style.display = "none";
		}
	}
}